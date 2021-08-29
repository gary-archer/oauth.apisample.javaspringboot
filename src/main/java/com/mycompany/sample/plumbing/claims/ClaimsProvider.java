package com.mycompany.sample.plumbing.claims;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.sample.plumbing.errors.ErrorCodes;
import com.mycompany.sample.plumbing.errors.ErrorFactory;
import com.nimbusds.jwt.JWTClaimsSet;

/*
 * The claims provider class is responsible for providing the final claims object
 */
public class ClaimsProvider {

    /*
     * The StandardAuthorizer calls this method, when all claims are included in the access token
     * These claims will have been collected earlier during token issuance by calling the ClaimsController
     */
    public final ApiClaims readClaims(final JWTClaimsSet tokenData) {

        return new ApiClaims(
                new BaseClaims(tokenData),
                new UserInfoClaims(tokenData),
                this.readCustomClaims(tokenData));
    }

    /*
     * The ClaimsCachingAuthorizer calls this, to ask the API to supply its claims when the token is first received
     */
    public final ApiClaims supplyClaims(final JWTClaimsSet tokenData, final JWTClaimsSet userInfoData) {

        var customClaims = this.supplyCustomClaims(tokenData, userInfoData);
        return new ApiClaims(
                new BaseClaims(tokenData),
                new UserInfoClaims(userInfoData),
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
            var custom = this.deserializeCustomClaims(data.get("custom"));
            return new ApiClaims(token, userInfo, custom);

        } catch (Throwable ex) {

            // Report the error including an error code and exception details
            throw ErrorFactory.createServerError(
                    ErrorCodes.JSON_PARSE_ERROR,
                    "Problem encountered parsing JSON claims data",
                    ex);
        }
    }

    /*
     * This default implementation can be overridden by derived classes
     */
    @SuppressWarnings(value = "checkstyle:DesignForExtension")
    protected CustomClaims readCustomClaims(final JWTClaimsSet token) {
        return new CustomClaims();
    }

    /*
     * This default implementation can be overridden by derived classes
     */
    @SuppressWarnings(value = "checkstyle:DesignForExtension")
    protected CustomClaims supplyCustomClaims(final JWTClaimsSet token, final JWTClaimsSet userInfo) {
        return new CustomClaims();
    }

    /*
     * This default implementation can be overridden to manage deserialization
     */
    @SuppressWarnings(value = "checkstyle:DesignForExtension")
    protected CustomClaims deserializeCustomClaims(final JsonNode claimsNode) {
        return CustomClaims.importData(claimsNode);
    }
}
