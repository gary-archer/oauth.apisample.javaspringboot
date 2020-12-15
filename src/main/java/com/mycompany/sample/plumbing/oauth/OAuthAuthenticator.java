package com.mycompany.sample.plumbing.oauth;

import java.net.URI;
import java.util.Arrays;
import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import com.mycompany.sample.plumbing.claims.CoreApiClaims;
import com.mycompany.sample.plumbing.configuration.OAuthConfiguration;
import com.mycompany.sample.plumbing.dependencies.CustomRequestScope;
import com.mycompany.sample.plumbing.errors.ErrorFactory;
import com.mycompany.sample.plumbing.errors.ErrorUtils;
import com.mycompany.sample.plumbing.logging.LogEntry;
import com.mycompany.sample.plumbing.logging.PerformanceBreakdown;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.TokenIntrospectionErrorResponse;
import com.nimbusds.oauth2.sdk.TokenIntrospectionRequest;
import com.nimbusds.oauth2.sdk.TokenIntrospectionResponse;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.UserInfoErrorResponse;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;

/*
 * The class from which OAuth calls are initiated
 */
@Component
@Scope(value = CustomRequestScope.NAME)
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class OAuthAuthenticator {

    private final OAuthConfiguration configuration;
    private final IssuerMetadata metadata;
    private final LogEntry logEntry;

    public OAuthAuthenticator(
            final OAuthConfiguration configuration,
            final IssuerMetadata metadata,
            final LogEntry logEntry) {

        this.configuration = configuration;
        this.metadata = metadata;
        this.logEntry = logEntry;
    }

    /*
     * Do OAuth work to perform token validation and user info lookup
     */
    public void validateTokenAndGetClaims(
            final String accessToken,
            final HttpServletRequest request,
            final CoreApiClaims claims) {

        // Create a child log entry for authentication related work
        // This ensures that any errors and performances in this area are reported separately to business logic
        var authorizationLogEntry = this.logEntry.createChild("authorizer");

        // Aim to use introspection to validate the token and read token claims
        var introspectionUri = metadata.getMetadata().getIntrospectionEndpointURI();
        if (introspectionUri != null
            && !StringUtils.isEmpty(this.configuration.getClientId())
            && !StringUtils.isEmpty(this.configuration.getClientSecret())) {

            // Use introspection if we can
            this.introspectTokenAndGetTokenClaims(accessToken, claims, introspectionUri);
        } else {

            // Otherwise use in memory token validation
            this.validateTokenInMemoryAndGetTokenClaims(accessToken, claims);
        }

        // It then adds user info claims
        this.getUserInfoClaims(accessToken, claims);

        // Finish logging here, and on exception the child is disposed by logging classes
        authorizationLogEntry.close();
    }

    /*
     * Validate the access token via introspection and populate claims
     */
    private void introspectTokenAndGetTokenClaims(
            final String accessToken,
            final CoreApiClaims claims,
            final URI introspectionUri) {

        try (var breakdown = this.logEntry.createPerformanceBreakdown("validateToken")) {

            // Supply the API's introspection credentials
            var introspectionClientId = new ClientID(this.configuration.getClientId());
            var introspectionClientSecret = new Secret(this.configuration.getClientSecret());
            var credentials = new ClientSecretBasic(introspectionClientId, introspectionClientSecret);

            // Set up the request
            var request = new TokenIntrospectionRequest(
                    introspectionUri,
                    credentials,
                    new BearerAccessToken(accessToken))
                        .toHTTPRequest();
            request.setAccept("application/json");

            // Make the request and get the response
            HTTPResponse httpResponse = request.send();

            // Handle errors returned in the response body and return an understandable error
            var introspectionResponse = TokenIntrospectionResponse.parse(httpResponse);
            if (!introspectionResponse.indicatesSuccess()) {
                var errorResponse = TokenIntrospectionErrorResponse.parse(httpResponse);
                throw ErrorUtils.fromIntrospectionError(errorResponse.getErrorObject(), introspectionUri.toString());
            }

            // Get token claims from the response
            var tokenClaims = introspectionResponse.toSuccessResponse();

            // We will return a 401 if the token is invalid or expired
            if (!tokenClaims.isActive()) {
                throw ErrorFactory.createClient401Error("Access token is expired and failed introspection");
            }

            // Define some callbacks to receive claims
            Function<String, String> getStringIntrospectionClaim =
                    (String name) -> tokenClaims.getStringParameter(name);
            Function<String, Integer> getIntegerIntrospectionClaim = (String name) -> {
                var value = tokenClaims.getNumberParameter(name);
                return (value != null) ? value.intValue() : null;
            };

            // Get token claims and use the immutable user id as the subject claim
            var subject = this.getStringClaim(getStringIntrospectionClaim, "sub");
            var clientId = this.getStringClaim(getStringIntrospectionClaim, "client_id");
            var scopes = this.getStringClaim(getStringIntrospectionClaim, "scope").split(" ");
            var expiry = this.getIntegerClaim(getIntegerIntrospectionClaim, "exp");

            // Make an audience check to ensure that the token is for this API
            this.verifyScopes(scopes);

            // Update token claims
            claims.setTokenInfo(subject, clientId, scopes, expiry);

        } catch (Throwable e) {

            // Report exceptions
            throw ErrorUtils.fromIntrospectionError(e, introspectionUri.toString());
        }
    }

    /*
     * Validate the access token in memory via the token signing public key
     */
    private void validateTokenInMemoryAndGetTokenClaims(final String accessToken, final CoreApiClaims claims) {

        try (var breakdown = this.logEntry.createPerformanceBreakdown("validateToken")) {

            // First get the access token header's kid value
            var jwt = this.decodeAccessToken(accessToken);
            var kid = jwt.getHeader().getKeyID();

            // Download the token signing public key
            var publicKey = this.getTokenSigningPublicKey(kid, breakdown);

            // Verify the token's digital signature
            this.validateJsonWebToken(jwt, publicKey, breakdown);

            // Define some callbacks to receive claims
            var tokenClaims = jwt.getPayload().toJSONObject();
            Function<String, String> getStringJwtClaim = tokenClaims::getAsString;
            Function<String, Integer> getIntegerJwtClaim = (String name) -> tokenClaims.getAsNumber(name).intValue();

            // Get token claims and use the immutable user id as the subject claim
            var subject = this.getStringClaim(getStringJwtClaim, "sub");
            var clientId = this.getStringClaim(getStringJwtClaim, "client_id");
            var scopes = this.getStringClaim(getStringJwtClaim, "scope").split(" ");
            var expiry = this.getIntegerClaim(getIntegerJwtClaim, "exp");

            // Make an audience check to ensure that the token is for this API
            this.verifyScopes(scopes);

            // Update token claims
            claims.setTokenInfo(subject, clientId, scopes, expiry);
        }
    }

    /*
     * Decode the JWT and get its key identifier
     */
    private SignedJWT decodeAccessToken(final String accessToken) {

        try {
            return SignedJWT.parse(accessToken);

        } catch (Throwable e) {
            throw ErrorUtils.fromAccessTokenDecodeError(e);
        }
    }

    /*
     * Get the public key with which our access token is signed
     */
    private JWK getTokenSigningPublicKey(final String keyIdentifier, final PerformanceBreakdown parent) {

        try (var breakdown = parent.createChild("getTokenSigningPublicKey")) {

            var jwksUri = this.metadata.getMetadata().getJWKSetURI();
            try {

                // Download token signing keys
                JWKSet keys = JWKSet.load(jwksUri.toURL());

                // Get the key that matches the JWT
                var publicKey = keys.getKeyByKeyId(keyIdentifier);

                // Check we have the expected RSA key
                if (!(publicKey instanceof RSAKey)) {
                    throw ErrorUtils.fromInvalidTokenSignatureType(publicKey.getClass().getName());
                }

                // Return the result
                return publicKey.toPublicJWK();

            } catch (Throwable e) {

                // Report exceptions
                throw ErrorUtils.fromTokenSigningKeysDownloadError(e, jwksUri.toString());
            }
        }
    }

    /*
     * Do the work of verifying the access token
     */
    private void validateJsonWebToken(final SignedJWT jwt, final JWK publicKey, final PerformanceBreakdown parent) {

        try (var breakdown = parent.createChild("validateJsonWebToken")) {

            JWSVerifier verifier = new RSASSAVerifier((RSAKey) publicKey);
            if (!jwt.verify(verifier)) {
                throw ErrorUtils.fromAccessTokenValidationError(null);
            }
        } catch (Throwable e) {

            // Report exceptions
            throw ErrorUtils.fromAccessTokenValidationError(e);
        }
    }

    /*
     * Make sure the token is for this API
     */
    private void verifyScopes(final String[] scopes) {

        var found = Arrays.stream(scopes).filter(s -> s.equals(this.configuration.getRequiredScope())).findFirst();
        if (found.isEmpty()) {
            throw ErrorFactory.createClient401Error("Access token does not have a valid scope for this API");
        }
    }

    /*
     * Perform OAuth user info lookup
     */
    private void getUserInfoClaims(final String accessToken, final CoreApiClaims claims) {

        var url = this.metadata.getMetadata().getUserInfoEndpointURI();
        try (var breakdown = this.logEntry.createPerformanceBreakdown("userInfoLookup")) {

            // Make the request
            HTTPResponse httpResponse = new UserInfoRequest(url, new BearerAccessToken(accessToken))
                    .toHTTPRequest()
                    .send();

            // Handle errors returned in the response body
            var userInfoResponse = UserInfoResponse.parse(httpResponse);
            if (!userInfoResponse.indicatesSuccess()) {
                var errorResponse = UserInfoErrorResponse.parse(httpResponse);
                throw ErrorUtils.fromUserInfoError(errorResponse.getErrorObject(), url.toString());
            }

            // Get token claims from the response
            var userInfo = userInfoResponse.toSuccessResponse().getUserInfo();
            Function<String, String> getUserInfoClaim = (String name) -> userInfo.getStringClaim(name);

            // Update claims
            var givenName = this.getStringClaim(getUserInfoClaim, UserInfo.GIVEN_NAME_CLAIM_NAME);
            var familyName = this.getStringClaim(getUserInfoClaim, UserInfo.FAMILY_NAME_CLAIM_NAME);
            var email = this.getStringClaim(getUserInfoClaim, UserInfo.EMAIL_CLAIM_NAME);
            claims.setUserInfo(givenName, familyName, email);

        } catch (Throwable e) {

            // Report exceptions
            throw ErrorUtils.fromUserInfoError(e, url.toString());
        }
    }

    /*
     * Do basic null checking of input when reading claims
     */
    private String getStringClaim(final Function<String, String> callback, final String name) {

        var claim = callback.apply(name);
        if (StringUtils.isEmpty(claim)) {
            throw ErrorUtils.fromMissingClaim(name);
        }

        return claim;
    }

    /*
     * Do basic null checking of input when reading claims
     */
    private int getIntegerClaim(final Function<String, Integer> callback, final String name) {

        var claim = callback.apply(name);
        if (claim == null) {
            throw ErrorUtils.fromMissingClaim(name);
        }

        return claim;
    }
}
