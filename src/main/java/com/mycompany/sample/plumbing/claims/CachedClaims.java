package com.mycompany.sample.plumbing.claims;

import lombok.Getter;

/*
 * Claims that are cached between API requests
 */
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class CachedClaims {

    @Getter
    private final UserInfoClaims userInfo;

    @Getter
    private final CustomClaims custom;

    public CachedClaims(
            final UserInfoClaims userInfo,
            final CustomClaims custom) {

        this.userInfo = userInfo;
        this.custom = custom;
    }
}
