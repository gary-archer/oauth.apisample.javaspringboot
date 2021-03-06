package com.mycompany.sample.plumbing.oauth;

import java.net.URI;
import java.text.ParseException;
import java.util.Arrays;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import com.mycompany.sample.plumbing.claims.TokenClaims;
import com.mycompany.sample.plumbing.claims.UserInfoClaims;
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
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.TokenIntrospectionErrorResponse;
import com.nimbusds.oauth2.sdk.TokenIntrospectionRequest;
import com.nimbusds.oauth2.sdk.TokenIntrospectionResponse;
import com.nimbusds.oauth2.sdk.TokenIntrospectionSuccessResponse;
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
    public TokenClaims validateToken(final String accessToken) {

        // See whether to use introspection
        var introspectionUri = metadata.getMetadata().getIntrospectionEndpointURI();
        if (introspectionUri != null
            && StringUtils.hasLength(this.configuration.getClientId())
            && StringUtils.hasLength(this.configuration.getClientSecret())) {

            // Use introspection if we can
            return this.introspectTokenAndGetTokenClaims(accessToken, introspectionUri);

        } else {

            // Otherwise use in memory token validation
            return this.validateTokenInMemoryAndGetTokenClaims(accessToken);
        }
    }

    /*
     * Perform OAuth user info lookup
     */
    public UserInfoClaims getUserInfo(final String accessToken) {

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

            // Get claims from the response
            var userInfo = userInfoResponse.toSuccessResponse().getUserInfo();

            // Get and return claims
            var givenName = this.getStringClaim(userInfo, UserInfo.GIVEN_NAME_CLAIM_NAME);
            var familyName = this.getStringClaim(userInfo, UserInfo.FAMILY_NAME_CLAIM_NAME);
            var email = this.getStringClaim(userInfo, UserInfo.EMAIL_CLAIM_NAME);
            return new UserInfoClaims(givenName, familyName, email);

        } catch (Throwable e) {

            // Report exceptions
            throw ErrorUtils.fromUserInfoError(e, url.toString());
        }
    }

    /*
     * Validate the access token via introspection and populate claims
     */
    private TokenClaims introspectTokenAndGetTokenClaims(
            final String accessToken,
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

            // Get token claims and use the immutable user id as the subject claim
            var subject = this.getStringClaim(tokenClaims, "sub");
            var clientId = this.getStringClaim(tokenClaims, "client_id");
            var scopes = this.getStringClaim(tokenClaims, "scope").split(" ");
            var expiry = (int) tokenClaims.getExpirationTime().toInstant().getEpochSecond();

            // Make an audience check to ensure that the token is for this API
            this.verifyScopes(scopes);

            // Return token claims
            return new TokenClaims(subject, clientId, scopes, expiry);

        } catch (Throwable e) {

            // Report exceptions
            throw ErrorUtils.fromIntrospectionError(e, introspectionUri.toString());
        }
    }

    /*
     * Validate the access token in memory via the token signing public key
     */
    private TokenClaims validateTokenInMemoryAndGetTokenClaims(final String accessToken) {

        try (var breakdown = this.logEntry.createPerformanceBreakdown("validateToken")) {

            // First get the access token header's kid value
            var jwt = this.decodeAccessToken(accessToken);
            var kid = jwt.getHeader().getKeyID();

            // Download the token signing public key
            var publicKey = this.getTokenSigningPublicKey(kid, breakdown);

            // Verify the token's digital signature and get its claims
            var tokenClaims = this.validateJsonWebToken(jwt, publicKey, breakdown);

            // Get token claims and use the immutable user id as the subject claim
            var subject = this.getStringClaim(tokenClaims, "sub");
            var clientId = this.getStringClaim(tokenClaims, "client_id");
            var scopes = this.getStringClaim(tokenClaims, "scope").split(" ");
            var expiry = (int) tokenClaims.getExpirationTime().toInstant().getEpochSecond();

            // Make an audience check to ensure that the token is for this API
            this.verifyScopes(scopes);

            // Update token claims
            return new TokenClaims(subject, clientId, scopes, expiry);
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
                if (!(publicKey instanceof RSAKey)) {

                    // Fail if not found or the wrong type
                    String message = String.format("Key with identifier: %s not found in JWKS download", keyIdentifier);
                    throw ErrorFactory.createClient401Error(message);
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
    private JWTClaimsSet validateJsonWebToken(
            final SignedJWT jwt,
            final JWK publicKey,
            final PerformanceBreakdown parent) {

        try (var breakdown = parent.createChild("validateJsonWebToken")) {

            JWSVerifier verifier = new RSASSAVerifier((RSAKey) publicKey);
            if (!jwt.verify(verifier)) {
                throw ErrorUtils.fromAccessTokenValidationError(null);
            }

            return jwt.getJWTClaimsSet();

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
     * Get a string claims from the introspection object
     */
    private String getStringClaim(final TokenIntrospectionSuccessResponse claims, final String name) {

        var claim = claims.getStringParameter(name);
        if (StringUtils.hasLength(claim)) {
            return claim;
        }

        throw ErrorUtils.fromMissingClaim(name);
    }

    /*
     * Get a string claims from the JWT claims object
     */
    private String getStringClaim(final JWTClaimsSet claims, final String name) {

        try {

            var claim = claims.getStringClaim(name);
            if (StringUtils.hasLength(claim)) {
                return claim;
            }

            throw ErrorUtils.fromMissingClaim(name);

        } catch (ParseException ex) {
            throw ErrorUtils.fromMissingClaim(name);
        }
    }

    /*
     * Get a string claims from the user info claims object
     */
    private String getStringClaim(final UserInfo claims, final String name) {

        var claim = claims.getStringClaim(name);
        if (StringUtils.hasLength(claim)) {
            return claim;
        }

        throw ErrorUtils.fromMissingClaim(name);
    }
}
