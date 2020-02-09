package com.mycompany.sample.host.claims;

import com.mycompany.sample.framework.api.base.security.CoreApiClaims;
import lombok.Getter;
import lombok.Setter;

/*
 * Override the core claims to support additional custom claims
 */
public class SampleApiClaims extends CoreApiClaims {

    // Our sample user will cover 2 of the 3 regions in the data being accessed
    @Getter
    @Setter
    private String[] regionsCovered;
}
