package com.mycompany.api.basicapi.entities;

import com.mycompany.api.basicapi.framework.oauth.CoreApiClaims;
import lombok.Getter;
import lombok.Setter;

/*
 * Override the core claims to support additional custom claims
 */
public class BasicApiClaims extends CoreApiClaims {

    // Product specific data for authorization
    @Getter
    @Setter
    private int[] accountsCovered;
}
