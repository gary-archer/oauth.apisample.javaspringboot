package com.authsamples.api.plumbing.claims;

import org.jose4j.jwt.JwtClaims;

/*
 * Add extra claims that you cannot, or do not want to, manage in the authorization server
 */
public interface ExtraClaimsProvider {

    /*
     * Get extra claims from the API's own data
     */
    Object lookupExtraClaims(JwtClaims jwtClaims);

    /*
     * Get extra claims from the cache
     */
    Object deserializeFromCache(String json);
}
