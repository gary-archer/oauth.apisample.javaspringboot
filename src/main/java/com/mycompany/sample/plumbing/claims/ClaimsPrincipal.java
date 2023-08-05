package com.mycompany.sample.plumbing.claims;

import org.jose4j.jwt.JwtClaims;
import org.springframework.security.core.AuthenticatedPrincipal;
import lombok.Getter;

/*
 * The total set of claims for this API
 */
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class ClaimsPrincipal implements AuthenticatedPrincipal {

    @Getter
    private final JwtClaims jwtClaims;

    @Getter
    private final CustomClaims customClaims;

    public ClaimsPrincipal(final JwtClaims jwtClaims, final CustomClaims customClaims) {
        this.jwtClaims = jwtClaims;
        this.customClaims = customClaims;
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
