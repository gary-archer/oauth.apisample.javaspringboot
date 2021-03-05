package com.mycompany.sample.host.claims;

import com.mycompany.sample.plumbing.claims.ApiClaims;
import lombok.Getter;
import lombok.Setter;

/*
 * Extend core claims for this particular API
 */
public class SampleApiClaims extends ApiClaims {

    @Getter
    @Setter
    private boolean isAdmin;

    @Getter
    @Setter
    private String[] regionsCovered;
}
