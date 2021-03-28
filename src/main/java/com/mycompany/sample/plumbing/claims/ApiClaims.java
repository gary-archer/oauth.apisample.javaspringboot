package com.mycompany.sample.plumbing.claims;

import org.springframework.security.core.AuthenticatedPrincipal;
import lombok.Getter;

/*
 * An extensible claims object for APIs
 */
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class ApiClaims implements AuthenticatedPrincipal {

    @Getter
    private final BaseClaims _token;

    @Getter
    private final UserInfoClaims _userInfo;

    @Getter
    private final CustomClaims _custom;

    public ApiClaims(
            final BaseClaims token,
            final UserInfoClaims userInfo,
            final CustomClaims custom) {

        this._token = token;
        this._userInfo = userInfo;
        this._custom = custom;
    }

    // Use the access token subject claim as the technical user name
    @Override
    public String getName() {
        return this._token.get_subject();
    }
}
