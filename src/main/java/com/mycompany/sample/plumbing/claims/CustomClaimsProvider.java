package com.mycompany.sample.plumbing.claims;

import org.jose4j.jwt.JwtClaims;
import com.fasterxml.jackson.databind.JsonNode;

/*
 * A class to deal with domain specific claims, needed for business authorization
 */
public class CustomClaimsProvider {

    /*
     * When using the StandardAuthorizer this is overridden at the time of token issuance
     */
    @SuppressWarnings(value = "checkstyle:DesignForExtension")
    public CustomClaims issue(final String subject, final String email) {
        return new CustomClaims();
    }

    /*
     * When using the StandardAuthorizer this is overridden to read claims from a received JWT
     */
    @SuppressWarnings(value = "checkstyle:DesignForExtension")
    public CustomClaims getFromPayload(final JwtClaims payload) {
        return new CustomClaims();
    }

    /*
     * When using the ClaimsCaching authorizer this gets custom claims when a token is first received
     */
    @SuppressWarnings(value = "checkstyle:DesignForExtension")
    public CustomClaims getFromLookup(final String accessToken, final BaseClaims baseClaims, final UserInfoClaims userInfo) {
        return new CustomClaims();
    }

    /*
     * When using the ClaimsCaching authorizer this manages deserialization from the cache
     */
    @SuppressWarnings(value = "checkstyle:DesignForExtension")
    public CustomClaims deserialize(final JsonNode data) {
        return CustomClaims.importData(data);
    }
}
