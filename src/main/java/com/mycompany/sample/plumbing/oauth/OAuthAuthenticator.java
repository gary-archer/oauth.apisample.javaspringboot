package com.mycompany.sample.plumbing.oauth;

import com.mycompany.sample.plumbing.configuration.OAuthConfiguration;
import com.mycompany.sample.plumbing.errors.ErrorFactory;
import com.mycompany.sample.plumbing.errors.ErrorUtils;
import com.mycompany.sample.plumbing.logging.LogEntry;
import com.mycompany.sample.plumbing.claims.CoreApiClaims;
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
import com.nimbusds.openid.connect.sdk.claims.ClaimsSet;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.annotation.RequestScope;

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
    public int authenticateAndSetClaims(
            final String accessToken,
            final HttpServletRequest request,
            final CoreApiClaims claims) {

        // Create a child log entry for authentication related work
        // This ensures that any errors and performances in this area are reported separately to business logic
        var authorizationLogEntry = this.logEntry.createChild("authorizer");

        // Our implementation introspects the token to get token claims
        var expiry = this.introspectTokenAndSetTokenClaims(accessToken, claims);

        // It then adds user info claims
        this.setCentralUserInfoClaims(accessToken, claims);

        // Finish logging here, and on exception the child is disposed by logging classes
        authorizationLogEntry.close();

        // It then returns the token expiry as a cache time to live
        return expiry;
    }

    /*
     * The entry point for validating an access token
     */
    private int introspectTokenAndSetTokenClaims(final String accessToken, final CoreApiClaims claims) {

        var url = this.metadata.getMetadata().getIntrospectionEndpointURI();
        try (var perf = this.logEntry.createPerformanceBreakdown("validateToken")) {

            // Supply the API's introspection credentials
            var introspectionClientId = new ClientID(this.configuration.getClientId());
            var introspectionClientSecret = new Secret(this.configuration.getClientSecret());
            var credentials = new ClientSecretBasic(introspectionClientId, introspectionClientSecret);

            // Make the request
            HTTPResponse httpResponse = new TokenIntrospectionRequest(
                    url,
                    credentials,
                    new BearerAccessToken(accessToken))
                        .toHTTPRequest()
                        .send();

            // Handle errors returned in the response body and return an understandable error
            var introspectionResponse = TokenIntrospectionResponse.parse(httpResponse);
            if (!introspectionResponse.indicatesSuccess()) {
                var errorResponse = TokenIntrospectionErrorResponse.parse(httpResponse);
                throw ErrorUtils.fromIntrospectionError(errorResponse.getErrorObject(), url.toString());
            }

            // Get token claims from the response
            var tokenClaims = introspectionResponse.toSuccessResponse();

            // We will return a 401 if the token is invalid or expired
            if (!tokenClaims.isActive()) {
                throw ErrorFactory.create401Error("Access token is expired and failed introspection");
            }

            // Get token claims and use the immutable user id as the subject claim
            var userId = this.getTokenStringClaim(tokenClaims, "uid");
            var clientId = this.getTokenStringClaim(tokenClaims, "client_id");
            var scope = this.getTokenStringClaim(tokenClaims, "scope");
            claims.setTokenInfo(userId, clientId, scope.split(" "));

            // Return the expiry time as the response
            return this.getTokenIntegerClaim(tokenClaims, "exp");

        } catch (Throwable e) {

            // Report exceptions
            throw ErrorUtils.fromIntrospectionError(e, url.toString());
        }
    }

    /*
     * The entry point for user lookup
     */
    private void setCentralUserInfoClaims(final String accessToken, final CoreApiClaims claims) {

        var url = this.metadata.getMetadata().getUserInfoEndpointURI();
        try (var perf = this.logEntry.createPerformanceBreakdown("userInfoLookup")) {

            // Make the request
            HTTPResponse httpResponse = new UserInfoRequest(url, new BearerAccessToken(accessToken))
                    .toHTTPRequest()
                    .send();

            // Handle a race condition where the access token expires just after introspection
            if (httpResponse.getStatusCode() == HttpStatus.UNAUTHORIZED.value()) {
                throw ErrorFactory.create401Error("Expired access token used for user info lookup");
            }

            // Handle errors returned in the response body
            var userInfoResponse = UserInfoResponse.parse(httpResponse);
            if (!userInfoResponse.indicatesSuccess()) {
                var errorResponse = UserInfoErrorResponse.parse(httpResponse);
                throw ErrorUtils.fromUserInfoError(errorResponse.getErrorObject(), url.toString());
            }

            // Get token claims from the response
            var userInfo = userInfoResponse.toSuccessResponse().getUserInfo();

            // Update claims
            var givenName = this.getUserInfoClaim(userInfo, UserInfo.GIVEN_NAME_CLAIM_NAME);
            var familyName = this.getUserInfoClaim(userInfo, UserInfo.FAMILY_NAME_CLAIM_NAME);
            var email = this.getUserInfoClaim(userInfo, UserInfo.EMAIL_CLAIM_NAME);
            claims.setCentralUserInfo(givenName, familyName, email);

        } catch (Throwable e) {

            // Report exceptions
            throw ErrorUtils.fromUserInfoError(e, url.toString());
        }
    }

    /*
     * Do basic null checking of input when reading token claims to avoid possible null pointer exceptions
     */
    private String getTokenStringClaim(final TokenIntrospectionSuccessResponse claims, final String name) {

        var claim = claims.getStringParameter(name);
        if (StringUtils.isEmpty(claim)) {
            throw ErrorUtils.fromMissingClaim(name);
        }

        return claim;
    }

    /*
     * Do basic null checking of input when reading token claims to avoid possible null pointer exceptions
     */
    private int getTokenIntegerClaim(final TokenIntrospectionSuccessResponse claims, final String name) {

        var claim = claims.getNumberParameter(name);
        if (claim == null) {
            throw ErrorUtils.fromMissingClaim(name);
        }

        return claim.intValue();
    }

    /*
     * Do basic null checking of input when reading user info claims to avoid possible null pointer exceptions
     */
    private String getUserInfoClaim(final ClaimsSet claims, final String name) {

        var claim = claims.getClaim(name, String.class);
        if (StringUtils.isEmpty(claim)) {
            throw ErrorUtils.fromMissingClaim(name);
        }

        return claim;
    }
}
