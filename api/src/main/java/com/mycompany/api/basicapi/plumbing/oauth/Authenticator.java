package com.mycompany.api.basicapi.plumbing.oauth;

import com.mycompany.api.basicapi.plumbing.errors.ErrorHandler;
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
import org.javatuples.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

/*
 * The class from which OAuth calls are initiated
 */
public class Authenticator {

    /*
     * The injected dependencies
     */
    private final OauthConfiguration configuration;
    private final IssuerMetadata metadata;

    /*
     * Receive dependencies
     */
    public Authenticator(OauthConfiguration configuration, IssuerMetadata metadata) {
        this.configuration = configuration;
        this.metadata = metadata;
    }

    /*
     * The entry point for validating an access token
     */
    public Pair<Boolean, Number> validateTokenAndSetClaims(String accessToken, CoreApiClaims claims) {

        var url = this.metadata.getMetadata().getIntrospectionEndpointURI();
        try {

            // Supply the API's introspection credentials
            var introspectionClientId = new ClientID(this.configuration.getClientId());
            var introspectionClientSecret = new Secret(this.configuration.getClientSecret());
            var credentials = new ClientSecretBasic(introspectionClientId, introspectionClientSecret);

            // Make the request
            HTTPResponse httpResponse = new TokenIntrospectionRequest(url, credentials, new BearerAccessToken(accessToken))
                    .toHTTPRequest()
                    .send();

            // Handle errors returned in the response body
            var introspectionResponse = TokenIntrospectionResponse.parse(httpResponse);
            if (!introspectionResponse.indicatesSuccess()) {
                var errorResponse = TokenIntrospectionErrorResponse.parse(httpResponse);
                throw ErrorHandler.fromIntrospectionError(errorResponse.getErrorObject(), url.toString());
            }

            // Get token claims from the response
            var tokenClaims = introspectionResponse.toSuccessResponse();

            // We will return a 401 if the token is invalid or expired
            if(!tokenClaims.isActive()) {
                return Pair.with(false, 0);
            }

            // Get token claims and use the immutable user id as the subject claim
            var userId = this.getTokenStringClaim(tokenClaims, "uid");
            var clientId = this.getTokenStringClaim(tokenClaims, "client_id");
            var scope = this.getTokenStringClaim(tokenClaims, "scope");
            claims.setTokenInfo(userId, clientId, scope.split(" "));

            // Return a success result
            var expiry = this.getTokenIntegerClaim(tokenClaims,"exp");
            return Pair.with(true, expiry);
        }
        catch(Exception e) {

            // Report errors
           throw ErrorHandler.fromIntrospectionError(e, url.toString());
        }
    }

    /*
     * The entry point for user lookup
     */
    public boolean setCentralUserInfoClaims(String accessToken, CoreApiClaims claims) {

        var url = this.metadata.getMetadata().getUserInfoEndpointURI();
        try {

            // Make the request
            HTTPResponse httpResponse = new UserInfoRequest(url, new BearerAccessToken(accessToken))
                    .toHTTPRequest()
                    .send();

            // Handle a race condition where the access token expires just after introspection
            if(httpResponse.getStatusCode() == HttpStatus.UNAUTHORIZED.value()) {
                return false;
            }

            // Handle errors returned in the response body
            var userInfoResponse = UserInfoResponse.parse(httpResponse);
            if (!userInfoResponse.indicatesSuccess()) {
                var errorResponse = UserInfoErrorResponse.parse(httpResponse);
                throw ErrorHandler.fromUserInfoError(errorResponse.getErrorObject(), url.toString());
            }

            // Get token claims from the response
            var userInfo = userInfoResponse.toSuccessResponse().getUserInfo();

            // Update claims
            var givenName = this.getUserInfoClaim(userInfo, UserInfo.GIVEN_NAME_CLAIM_NAME);
            var familyName = this.getUserInfoClaim(userInfo, UserInfo.FAMILY_NAME_CLAIM_NAME);
            var email = this.getUserInfoClaim(userInfo, UserInfo.EMAIL_CLAIM_NAME);
            claims.setCentralUserInfo(givenName, familyName, email);
            return true;
        }
        catch(Exception e) {

            // Report errors
            throw ErrorHandler.fromUserInfoError(e, url.toString());
        }
    }

    /*
     * Do basic null checking of input when reading token claims to avoid possible null pointer exceptions
     */
    private String getTokenStringClaim(TokenIntrospectionSuccessResponse claims, String name) {

        var claim = claims.getStringParameter(name);
        if(StringUtils.isEmpty(claim)) {
            throw ErrorHandler.fromMissingClaim(name);
        }

        return claim;
    }

    /*
     * Do basic null checking of input when reading token claims to avoid possible null pointer exceptions
     */
    private int getTokenIntegerClaim(TokenIntrospectionSuccessResponse claims, String name) {

        var claim = claims.getNumberParameter(name);
        if(claim == null) {
            throw ErrorHandler.fromMissingClaim(name);
        }

        return claim.intValue();
    }

    /*
     * Do basic null checking of input when reading user info claims to avoid possible null pointer exceptions
     */
    private String getUserInfoClaim(ClaimsSet claims, String name) {

        var claim = claims.getClaim(name, String.class);
        if(StringUtils.isEmpty(claim)) {
            throw ErrorHandler.fromMissingClaim(name);
        }

        return claim;
    }
}
