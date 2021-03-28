package com.mycompany.sample.plumbing.oauth;

import java.net.URI;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import com.mycompany.sample.plumbing.claims.ClaimsPayload;
import com.mycompany.sample.plumbing.configuration.OAuthConfiguration;
import com.mycompany.sample.plumbing.dependencies.CustomRequestScope;
import com.mycompany.sample.plumbing.errors.ErrorUtils;
import com.mycompany.sample.plumbing.logging.LogEntry;
import com.mycompany.sample.plumbing.oauth.tokenvalidation.TokenValidator;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.UserInfoErrorResponse;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;

/*
 * The entry point for calls to the Authorization Server
 */
@Component
@Scope(value = CustomRequestScope.NAME)
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class OAuthAuthenticator {

    private final OAuthConfiguration _configuration;
    private final TokenValidator _tokenValidator;
    private final LogEntry _logEntry;

    public OAuthAuthenticator(
            final OAuthConfiguration configuration,
            final TokenValidator tokenValidator,
            final LogEntry logEntry) {

        this._configuration = configuration;
        this._tokenValidator = tokenValidator;
        this._logEntry = logEntry;
    }

    /*
     * Do the work of validating the access token and returning its claims
     */
    public ClaimsPayload validateToken(final String accessToken) {

        try (var breakdown = this._logEntry.createPerformanceBreakdown("validateToken")) {
            return this._tokenValidator.validateToken(accessToken);
        }
    }

    /*
     * Perform OAuth user info lookup
     */
    public ClaimsPayload getUserInfo(final String accessToken) {

        try (var breakdown = this._logEntry.createPerformanceBreakdown("userInfoLookup")) {

            // Make the request
            var userInfoUrl = new URI(this._configuration.get_userInfoEndpoint());
            HTTPResponse httpResponse = new UserInfoRequest(userInfoUrl, new BearerAccessToken(accessToken))
                    .toHTTPRequest()
                    .send();

            // Handle errors returned in the response body
            var userInfoResponse = UserInfoResponse.parse(httpResponse);
            if (!userInfoResponse.indicatesSuccess()) {
                var errorResponse = UserInfoErrorResponse.parse(httpResponse);
                throw ErrorUtils.fromUserInfoError(
                        errorResponse.getErrorObject(),
                        this._configuration.get_userInfoEndpoint());
            }

            // Get claims from the response
            var userInfo = userInfoResponse.toSuccessResponse().getUserInfo();

            // Return a payload object that will be read later
            var payload = new ClaimsPayload(userInfo);
            payload.set_stringClaimCallback(this::getStringClaim);
            return payload;

        } catch (Throwable e) {

            // Report exceptions
            throw ErrorUtils.fromUserInfoError(e, this._configuration.get_userInfoEndpoint());
        }
    }

    /*
     * Get a string claims from the user info claims object
     */
    private String getStringClaim(final Object data, final String name) {

        var claimsSet = (UserInfo) data;
        var claim = claimsSet.getStringClaim(name);
        if (StringUtils.hasLength(claim)) {
            return claim;
        }

        throw ErrorUtils.fromMissingClaim(name);
    }
}
