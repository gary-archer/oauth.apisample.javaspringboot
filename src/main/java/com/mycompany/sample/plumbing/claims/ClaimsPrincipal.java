package com.mycompany.sample.plumbing.claims;

import org.springframework.security.core.AuthenticatedPrincipal;
import lombok.Getter;

/*
 * The total set of claims for this API
 */
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class ClaimsPrincipal implements AuthenticatedPrincipal {

    @Getter
    private final BaseClaims token;

    @Getter
    private final CustomClaims custom;

    public ClaimsPrincipal(final BaseClaims token, final CustomClaims custom) {
        this.token = token;
        this.custom = custom;
    }

    // Use the access token subject claim as the technical username
    @Override
    public String getName() {
        return this.token.getSubject();
    }
}
