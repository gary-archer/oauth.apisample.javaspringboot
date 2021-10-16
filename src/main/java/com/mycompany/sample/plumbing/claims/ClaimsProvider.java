package com.mycompany.sample.plumbing.claims;

import com.fasterxml.jackson.databind.JsonNode;

/*
 * A class to deal with domain specific claims, needed for business authorization
 */
public class ClaimsProvider {

    /*
     * This can be overridden by derived classes and is used at the time of token issuance
     */
    @SuppressWarnings(value = "checkstyle:DesignForExtension")
    public CustomClaims issue(final String subject) {
        return new CustomClaims();
    }

    /*
     * Alternatively, this can be overridden by derived classes to get custom claims when a token is first received
     */
    @SuppressWarnings(value = "checkstyle:DesignForExtension")
    public CustomClaims get(final String accessToken, final BaseClaims baseClaims, final UserInfoClaims userInfo) {
        return new CustomClaims();
    }

    /*
     * This can be overridden by derived classes
     */
    @SuppressWarnings(value = "checkstyle:DesignForExtension")
    protected CustomClaims deserialize(final JsonNode data) {
        return CustomClaims.importData(data);
    }
}
