package com.mycompany.sample.plumbing.claims;

import com.fasterxml.jackson.databind.JsonNode;

/*
 * A class to deal with domain specific claims, needed for business authorization
 */
public class CustomClaimsProvider {

    /*
     * Look up custom claims when details are not available in the cache, such as for a new access token
     */
    @SuppressWarnings(value = "checkstyle:DesignForExtension")
    public CustomClaims lookupForNewAccessToken(final String accessToken, final BaseClaims baseClaims) {
        return new CustomClaims();
    }

    /*
     * When custom claims are in the cache, deserialize them into an object
     */
    @SuppressWarnings(value = "checkstyle:DesignForExtension")
    public CustomClaims deserializeFromCache(final JsonNode data) {
        return CustomClaims.importData(data);
    }
}
