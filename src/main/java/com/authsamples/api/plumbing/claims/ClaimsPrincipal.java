package com.authsamples.api.plumbing.claims;

import org.jose4j.jwt.JwtClaims;
import lombok.Getter;

/*
 * The concrete claims for this API
 */
public class ClaimsPrincipal {

    /*
     * Verifiable claims from the access token
     */
    @Getter
    private final JwtClaims jwtClaims;

    /*
     * Additional authorization values that the API's business logic also treats like claims
     */
    @Getter
    private final ExtraClaims extraClaims;

    /*
     * The OAuth filter constructs the claims principal
     */
    public ClaimsPrincipal(final JwtClaims jwtClaims, final ExtraClaims extraClaims) {
        this.jwtClaims = jwtClaims;
        this.extraClaims = extraClaims;
    }

    /*
     * Return the subject claim as an anonymous user identifier
     */
    public String getSubject() {
        return ClaimsReader.getStringClaim(this.jwtClaims, "sub");
    }
}
