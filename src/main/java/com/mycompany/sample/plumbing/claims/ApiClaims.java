package com.mycompany.sample.plumbing.claims;

import org.springframework.security.core.AuthenticatedPrincipal;
import lombok.Getter;

/*
 * An extensible claims object for APIs
 */
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class ApiClaims implements AuthenticatedPrincipal {

    @Getter
    private final BaseClaims token;

    @Getter
    private final UserInfoClaims userInfo;

    @Getter
    private final CustomClaims custom;

    public ApiClaims(
            final BaseClaims token,
            final UserInfoClaims userInfo,
            final CustomClaims custom) {

        this.token = token;
        this.userInfo = userInfo;
        this.custom = custom;
    }

    // Use the access token subject claim as the technical user name
    @Override
    public String getName() {
        return this.token.getSubject();
    }
}
