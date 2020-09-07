package com.mycompany.sample.plumbing.oauth;

import java.net.URI;
import java.text.ParseException;
import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.TokenIntrospectionErrorResponse;
import com.nimbusds.oauth2.sdk.TokenIntrospectionRequest;
import com.nimbusds.oauth2.sdk.TokenIntrospectionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.annotation.RequestScope;
import com.mycompany.sample.plumbing.claims.CoreApiClaims;
import com.mycompany.sample.plumbing.configuration.OAuthConfiguration;
import com.mycompany.sample.plumbing.errors.ErrorFactory;
import com.mycompany.sample.plumbing.errors.ErrorUtils;
import com.mycompany.sample.plumbing.logging.LogEntry;
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
@RequestScope
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
     * Our form of authentication performs introspection and user info lookup
     */
    public void validateTokenAndGetClaims(
            final String accessToken,
            final HttpServletRequest request,
            final CoreApiClaims claims) {

        // Create a child log entry for authentication related work
        // This ensures that any errors and performances in this area are reported separately to business logic
        var authorizationLogEntry = this.logEntry.createChild("authorizer");

        // First validate the token and get token claims, using introspection if supported
        var introspectionUri = metadata.getMetadata().getIntrospectionEndpointURI();
        if (introspectionUri != null) {
            this.introspectTokenAndGetTokenClaims(accessToken, claims, introspectionUri);
        } else {
            this.validateTokenInMemoryAndGetTokenClaims(accessToken, claims);
        }

        // It then adds user info claims
        this.getCentralUserInfoClaims(accessToken, claims);

        // Finish logging here, and on exception the child is disposed by logging classes
        authorizationLogEntry.close();
    }

    /*
     * The entry point for validating an access token
     */
    private void introspectTokenAndGetTokenClaims(
            final String accessToken,
            final CoreApiClaims claims,
            final URI introspectionUri) {

        try (var perf = this.logEntry.createPerformanceBreakdown("validateToken")) {

            // Supply the API's introspection credentials
            var introspectionClientId = new ClientID(this.configuration.getClientId());
            var introspectionClientSecret = new Secret(this.configuration.getClientSecret());
            var credentials = new ClientSecretBasic(introspectionClientId, introspectionClientSecret);

            // Make the request
            HTTPResponse httpResponse = new TokenIntrospectionRequest(
                    introspectionUri,
                    credentials,
                    new BearerAccessToken(accessToken))
                        .toHTTPRequest()
                        .send();

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
            Function<String, String> getStringIntrospectionClaim = (String name) -> tokenClaims.getStringParameter(name);
            Function<String, Integer> getIntegerIntrospectionClaim = (String name) -> {
                var value = tokenClaims.getNumberParameter(name);
                return (value != null) ? value.intValue() : null;
            };

            // If required, also check the token's audience and scopes before accepting claims

            // Get token claims and use the immutable user id as the subject claim
            var subject = this.getStringClaim(getStringIntrospectionClaim, "uid");
            var clientId = this.getStringClaim(getStringIntrospectionClaim, "client_id");
            var scope = this.getStringClaim(getStringIntrospectionClaim, "scope");
            var expiry = this.getIntegerClaim(getIntegerIntrospectionClaim, "exp");
            claims.setTokenInfo(subject, clientId, scope.split(" "), expiry);

        } catch (Throwable e) {

            // Report exceptions
            throw ErrorUtils.fromIntrospectionError(e, introspectionUri.toString());
        }
    }

    /*
     * As above but uses in memory token validation
     */
    private void validateTokenInMemoryAndGetTokenClaims(final String accessToken, final CoreApiClaims claims) {

        try (var perf = this.logEntry.createPerformanceBreakdown("validateToken")) {

            // First decode the JWT and get its kid
            var decodedJwt = SignedJWT.parse(accessToken);
            var kid = decodedJwt.getHeader().getKeyID();

            // Download token signing keys
            var keysUri = this.metadata.getMetadata().getJWKSetURI();
            JWKSet publicKeys = JWKSet.load(keysUri.toURL());

            // Get the key that matches the JWT
            var publicKey = publicKeys.getKeyByKeyId(kid);
            if (publicKey == null) {
                throw new RuntimeException("PUBLIC KEY NOT FOUND");
            }

            // Check we have the expected RSA key
            if (!(publicKey instanceof RSAKey)) {
                throw new RuntimeException("UNEXPECTED KEY TYPE");
            }

            // Validate the token signature
            JWSVerifier verifier = new RSASSAVerifier((RSAKey) publicKey);
            if (!decodedJwt.verify(verifier)) {
                throw new RuntimeException("TOKEN VALIDATION FAILURE");
            }

            // If required, also check the token's audience and scopes before accepting claims

            // Define some callbacks to receive claims
            var tokenClaims = decodedJwt.getPayload().toJSONObject();
            Function<String, String> getStringJwtClaim = tokenClaims::getAsString;
            Function<String, Integer> getIntegerJwtClaim = (String name) -> tokenClaims.getAsNumber(name).intValue();

            // Get token claims and use the immutable user id as the subject claim
            var subject = this.getStringClaim(getStringJwtClaim, "sub");
            var clientId = this.getStringClaim(getStringJwtClaim, "client_id");
            var scope = this.getStringClaim(getStringJwtClaim, "scope");
            var expiry = this.getIntegerClaim(getIntegerJwtClaim, "exp");
            claims.setTokenInfo(subject, clientId, scope.split(" "), expiry);
        }
        catch (Throwable e) {

            // Report exceptions
            throw ErrorUtils.fromTokenValidationError(e);
        }
    }

    /*
     * The entry point for user lookup
     */
    private void getCentralUserInfoClaims(final String accessToken, final CoreApiClaims claims) {

        var url = this.metadata.getMetadata().getUserInfoEndpointURI();
        try (var perf = this.logEntry.createPerformanceBreakdown("userInfoLookup")) {

            // Make the request
            HTTPResponse httpResponse = new UserInfoRequest(url, new BearerAccessToken(accessToken))
                    .toHTTPRequest()
                    .send();

            // Handle a race condition where the access token expires just after introspection
            if (httpResponse.getStatusCode() == HttpStatus.UNAUTHORIZED.value()) {
                throw ErrorFactory.createClient401Error("Expired access token used for user info lookup");
            }

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
            claims.setCentralUserInfo(givenName, familyName, email);

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
