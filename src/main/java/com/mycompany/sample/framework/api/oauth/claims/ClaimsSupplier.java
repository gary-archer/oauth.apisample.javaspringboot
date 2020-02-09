package com.mycompany.sample.framework.api.oauth.claims;

import com.mycompany.sample.framework.api.base.security.CoreApiClaims;

/*
 * This class is injected into framework authentication handling
 * Due to Java type erasure the framework is unable to new up TClaims related items itself
 */
public interface ClaimsSupplier<TClaims extends CoreApiClaims>  {

    // A new empty claims is created then populated on every request with a new token
    TClaims createEmptyClaims();

    // A new custom claims provider is created then populated on every request with a new token
    CustomClaimsProvider<TClaims> createCustomClaimsProvider();
}
