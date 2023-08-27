package com.mycompany.sample.plumbing.claims;

import org.springframework.security.core.AuthenticatedPrincipal;
import lombok.Getter;

/*
 * The total set of claims for this API
 */
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class ClaimsPrincipal implements AuthenticatedPrincipal {

    @Getter
    private final TokenClaims tokenClaims;

    @Getter
    private final ExtraClaims extraClaims;

    public ClaimsPrincipal(final TokenClaims tokenClaims, final ExtraClaims extraClaims) {
        this.tokenClaims = tokenClaims;
        this.extraClaims = extraClaims;
    }

    /*
     * Use the access token subject claim as the technical name for the identity
     */
    @Override
    public String getName() {
        return this.tokenClaims.getSubject();
    }
}
