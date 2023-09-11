package com.mycompany.sample.plumbing.claims;

import org.jose4j.jwt.JwtClaims;
import com.fasterxml.jackson.databind.JsonNode;

/*
 * Add extra claims that you cannot, or do not want to, manage in the authorization server
 */
public class ExtraClaimsProvider {

    /*
     * Get additional claims from the API's own database
     */
    @SuppressWarnings(value = "checkstyle:DesignForExtension")
    public ExtraClaims lookupExtraClaims(final JwtClaims jwtClaims) {
        return new ExtraClaims();
    }

    /*
     * Create a claims principal that manages lookups across both token claims and extra claims
     */
    @SuppressWarnings(value = "checkstyle:DesignForExtension")
    public ClaimsPrincipal createClaimsPrincipal(final JwtClaims jwtClaims, final ExtraClaims extraClaims) {
        return new ClaimsPrincipal(jwtClaims, extraClaims);
    }

    /*
     * Deserialize extra claims after they have been read from the cache
     */
    @SuppressWarnings(value = "checkstyle:DesignForExtension")
    public ExtraClaims deserializeFromCache(final JsonNode data) {
        return ExtraClaims.importData(data);
    }
}
