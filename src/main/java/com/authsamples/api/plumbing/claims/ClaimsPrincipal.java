package com.authsamples.api.plumbing.claims;

import org.jose4j.jwt.JwtClaims;
import org.springframework.security.core.AuthenticatedPrincipal;
import lombok.Getter;

/*
 * The total set of claims for this API
 */
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class ClaimsPrincipal implements AuthenticatedPrincipal {

    /*
     * Verifiable claims from the access token
     */
    @Getter
    private final JwtClaims jwtClaims;

    /*
     * Additional authorization values that the API's business logic also treats like claims
     */
    @Getter
    private final Object extraClaims;

    /*
     * The OAuth filter constructs the claims principal
     */
    public ClaimsPrincipal(final JwtClaims jwtClaims, final Object extraClaims) {
        this.jwtClaims = jwtClaims;
        this.extraClaims = extraClaims;
    }

    /*
     * Return the subject claim
     */
    public String getSubject() {
        return ClaimsReader.getStringClaim(this.jwtClaims, "sub");
    }

    /*
     * Use the access token subject claim as the technical name for the identity
     */
    @Override
    public String getName() {
        return this.getSubject();
    }
}
