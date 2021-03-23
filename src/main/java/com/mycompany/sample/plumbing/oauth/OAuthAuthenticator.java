package com.mycompany.sample.plumbing.oauth;

import java.net.URI;
import java.text.ParseException;
import com.mycompany.sample.plumbing.oauth.tokenvalidation.ClaimsPayload;
import com.mycompany.sample.plumbing.oauth.tokenvalidation.TokenValidator;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import com.mycompany.sample.plumbing.claims.BaseClaims;
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
import com.nimbusds.oauth2.sdk.TokenIntrospectionSuccessResponse;
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
    public ClaimsPayload validateToken(final String accessToken) {

        try (var breakdown = this.logEntry.createPerformanceBreakdown("validateToken")) {
            return this.tokenValidator.validateToken(accessToken);
        }
    }

    /*
     * Perform OAuth user info lookup
     */
    public UserInfoClaims getUserInfo(final String accessToken) {

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
            var userInfo = userInfoResponse.toSuccessResponse().getUserInfo();

            // Get and return claims
            var givenName = this.getStringClaim(userInfo, UserInfo.GIVEN_NAME_CLAIM_NAME);
            var familyName = this.getStringClaim(userInfo, UserInfo.FAMILY_NAME_CLAIM_NAME);
            var email = this.getStringClaim(userInfo, UserInfo.EMAIL_CLAIM_NAME);
            return new UserInfoClaims(givenName, familyName, email);

        } catch (Throwable e) {

            // Report exceptions
            throw ErrorUtils.fromUserInfoError(e, this.configuration.getUserInfoEndpoint());
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
