package com.mycompany.sample.plumbing.oauth;

import java.net.URI;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.mycompany.sample.plumbing.claims.ClaimParser;
import com.mycompany.sample.plumbing.configuration.OAuthConfiguration;
import com.mycompany.sample.plumbing.dependencies.CustomRequestScope;
import com.mycompany.sample.plumbing.errors.ErrorUtils;
import com.mycompany.sample.plumbing.logging.LogEntry;
import com.mycompany.sample.plumbing.oauth.tokenvalidation.TokenValidator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.UserInfoErrorResponse;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;

/*
 * The entry point for calls to the Authorization Server
 */
@Component
@Scope(value = CustomRequestScope.NAME)
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class OAuthAuthenticator {

    private final OAuthConfiguration configuration;
    private final TokenValidator tokenValidator;
    private final LogEntry logEntry;

    public OAuthAuthenticator(
            final OAuthConfiguration configuration,
            final TokenValidator tokenValidator,
            final LogEntry logEntry) {

        this.configuration = configuration;
        this.tokenValidator = tokenValidator;
        this.logEntry = logEntry;
    }

    /*
     * Do the work of validating the access token and returning its claims
     */
    public JWTClaimsSet validateToken(final String accessToken) {

        try (var breakdown = this.logEntry.createPerformanceBreakdown("validateToken")) {
            return this.tokenValidator.validateToken(accessToken);
        }
    }

    /*
     * Perform OAuth user info lookup
     */
    public JWTClaimsSet getUserInfo(final String accessToken) {

        try (var breakdown = this.logEntry.createPerformanceBreakdown("userInfoLookup")) {

            // Make the request
            var userInfoUrl = new URI(this.configuration.getUserInfoEndpoint());
            HTTPResponse httpResponse = new UserInfoRequest(userInfoUrl, new BearerAccessToken(accessToken))
                    .toHTTPRequest()
                    .send();

            // Handle errors returned in the response body
            var userInfoResponse = UserInfoResponse.parse(httpResponse);
            if (!userInfoResponse.indicatesSuccess()) {
                var errorResponse = UserInfoErrorResponse.parse(httpResponse);
                throw ErrorUtils.fromUserInfoError(
                        errorResponse.getErrorObject(),
                        this.configuration.getUserInfoEndpoint());
            }

            // Get claims from the response
            var data = userInfoResponse.toSuccessResponse().getUserInfo();

            // Return a claims set object with results
            return new JWTClaimsSet.Builder()
                    .claim("given_name", ClaimParser.getStringClaim(data, "given_name"))
                    .claim("family_name", ClaimParser.getStringClaim(data, "family_name"))
                    .claim("email", ClaimParser.getStringClaim(data, "email"))
                    .build();

        } catch (Throwable e) {

            // Report exceptions
            throw ErrorUtils.fromUserInfoError(e, this.configuration.getUserInfoEndpoint());
        }
    }
}
