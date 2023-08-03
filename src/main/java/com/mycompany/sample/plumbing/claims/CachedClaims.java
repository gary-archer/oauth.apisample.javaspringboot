package com.mycompany.sample.plumbing.claims;

import lombok.Getter;

/*
 * Claims that are cached between API requests
 */
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class CachedClaims {

    @Getter
    private final CustomClaims custom;

    public CachedClaims(final CustomClaims custom) {
        this.custom = custom;
    }
}
