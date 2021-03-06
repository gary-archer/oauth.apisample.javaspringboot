package com.mycompany.sample.logic.entities;

import lombok.Getter;
import lombok.Setter;

/*
 * OAuth user info for returning to clients for display
 */
public class ClientUserInfo {

    @Getter
    @Setter
    private String givenName;

    @Getter
    @Setter
    private String familyName;
}
