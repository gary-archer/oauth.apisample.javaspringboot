package com.mycompany.sample.host.claims;

import lombok.Getter;
import lombok.Setter;

/*
 * User attributes stored in the authorization server
 */
public class IdentityClaims {

    @Getter
    @Setter
    private String subject;

    @Getter
    @Setter
    private String email;
}
