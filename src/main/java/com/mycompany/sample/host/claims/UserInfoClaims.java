package com.mycompany.sample.host.claims;

import lombok.Getter;

/*
 * User info is returned to the UI or used by the API
 */
public class UserInfoClaims {

    @Getter
    private final String givenName;

    @Getter
    private final String familyName;

    @Getter
    private final String email;

    /*
     * Initialize from input
     */
    public UserInfoClaims(final String givenName, final String familyName, final String email) {
        this.givenName = givenName;
        this.familyName = familyName;
        this.email = email;
    }
}
