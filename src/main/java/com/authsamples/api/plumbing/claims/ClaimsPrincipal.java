package com.authsamples.api.plumbing.claims;

import org.jose4j.jwt.JwtClaims;
import lombok.Getter;

/*
 * The claims for this API
 */
public class ClaimsPrincipal {

    /*
     * Verifiable claims from the access token
     */
    @Getter
    private final JwtClaims jwt;

    /*
     * Additional authorization values that the API's business logic also treats like claims
     */
    @Getter
    private final ExtraClaims extra;

    /*
     * The OAuth filter constructs the claims principal
     */
    public ClaimsPrincipal(final JwtClaims jwtClaims, final ExtraClaims extraClaims) {
        this.jwt = jwtClaims;
        this.extra = extraClaims;
    }

    /*
     * Return the subject claim as an anonymous user identifier
     */
    public String getSubject() {
        return ClaimsReader.getStringClaim(this.jwt, "sub");
    }
}
