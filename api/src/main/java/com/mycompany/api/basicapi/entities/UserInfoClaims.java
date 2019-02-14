package com.mycompany.api.basicapi.entities;

import lombok.Getter;

/*
 * User info is returned to the UI or used by the API
 */
public class UserInfoClaims {

    @Getter
    private String givenName;

    @Getter
    private String familyName;

    @Getter
    private String email;

    /*
     * Initialize from input
     */
    public UserInfoClaims(String givenName, String familyName, String email) {
        this.givenName = givenName;
        this.familyName = familyName;
        this.email = email;
    }
}