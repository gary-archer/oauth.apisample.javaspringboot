package com.mycompany.sample.plumbing.claims;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.sample.plumbing.errors.ErrorCodes;
import com.mycompany.sample.plumbing.errors.ErrorFactory;

/*
 * A default implementation that can be overridden
 */
public class CustomClaimsProvider {

    /*
     * The StandardAuthorizer calls this method, when all claims are included in the access token
     * These claims will have been collected earlier during token issuance by calling the ClaimsController
     */
    public final ApiClaims readClaims(final ClaimsPayload tokenData) {

        return new ApiClaims(
                this.readBaseClaims(tokenData),
                this.readUserInfoClaims(tokenData),
                this.readCustomClaims(tokenData));
    }

    /*
     * The ClaimsCachingAuthorizer calls this, to ask the API to supply its claims when the token is first received
     */
    public final ApiClaims supplyClaims(final ClaimsPayload tokenData, final ClaimsPayload userInfoData) {

        var customClaims = this.supplyCustomClaims(tokenData, userInfoData);

        return new ApiClaims(
                this.readBaseClaims(tokenData),
                this.readUserInfoClaims(userInfoData),
                customClaims);
    }

    /*
     * Do the serialization work before saving to the cache
     */
    public final String serializeToCache(final ApiClaims claims) {

        var mapper = new ObjectMapper();
        var data = mapper.createObjectNode();
        data.set("token", claims.getToken().exportData());
        data.set("userInfo", claims.getUserInfo().exportData());
        data.set("custom", claims.getCustom().exportData());
        return data.toString();
    }

    /*
     * Do the deserialization work to read claims from the cache
     */
    public final ApiClaims deserializeFromCache(final String claimsText) {

        try {
            var mapper = new ObjectMapper();
            var data = mapper.readValue(claimsText, ObjectNode.class);

            var token = BaseClaims.importData(data.get("token"));
            var userInfo = UserInfoClaims.importData(data.get("userInfo"));
            var custom = this.deserializeCustomClaimsFromCache(data.get("custom"));

            return new ApiClaims(token, userInfo, custom);

        } catch (Throwable ex) {

            // Report the error including an error code and exception details
            throw ErrorFactory.createServerError(
                    ErrorCodes.JSON_PARSE_ERROR,
                    "Problem encountered parsing JSON data",
                    ex);
        }
    }

    /*
     * This default implementation can be overridden by derived classes
     */
    @SuppressWarnings(value = "checkstyle:DesignForExtension")
    protected CustomClaims readCustomClaims(final ClaimsPayload token) {
        return new CustomClaims();
    }

    /*
     * This default implementation can be overridden by derived classes
     */
    @SuppressWarnings(value = "checkstyle:DesignForExtension")
    protected CustomClaims supplyCustomClaims(final ClaimsPayload token, final ClaimsPayload userInfo) {
        return new CustomClaims();
    }

    /*
     * This default implementation can be overridden to manage deserialization
     */
    @SuppressWarnings(value = "checkstyle:DesignForExtension")
    protected CustomClaims deserializeCustomClaimsFromCache(final JsonNode claimsNode) {
        return CustomClaims.importData(claimsNode);
    }

    /*
     * Read base claims from the supplied token data
     */
    private BaseClaims readBaseClaims(final ClaimsPayload data) {

        var subject = data.getStringClaim("sub");
        var scopes = data.getStringClaim("scope").split(" ");
        var expiry = data.getExpirationClaim();
        return new BaseClaims(subject, scopes, expiry);
    }

    /*
     * Read user info claims from the supplied data, which could originate from a token or user info payload
     */
    private UserInfoClaims readUserInfoClaims(final ClaimsPayload data) {

        var givenName = data.getStringClaim("given_name");
        var familyName = data.getStringClaim("family_name");
        var email = data.getStringClaim("email");
        return new UserInfoClaims(givenName, familyName, email);
    }
}
