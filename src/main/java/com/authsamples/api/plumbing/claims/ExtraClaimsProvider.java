package com.authsamples.api.plumbing.claims;

import org.jose4j.jwt.JwtClaims;

/*
 * Add extra claims that you cannot, or do not want to, manage in the authorization server
 */
public interface ExtraClaimsProvider<T> {

    /*
     * Get extra claims from the API's own data
     */
    T lookupExtraClaims(JwtClaims jwtClaims);

    /*
     * Create a custom claims principal
     */
    ClaimsPrincipal<T> createClaimsPrincipal(JwtClaims jwtClaims, Object extraClaims);

    /*
     * Load extra claims from the cache
     */
    T deserializeFromCache(String json);
}
