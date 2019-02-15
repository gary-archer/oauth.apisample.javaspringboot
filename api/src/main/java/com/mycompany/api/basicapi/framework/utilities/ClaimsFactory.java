package com.mycompany.api.basicapi.framework.utilities;

import com.mycompany.api.basicapi.framework.oauth.ClaimsCache;
import com.mycompany.api.basicapi.framework.oauth.CoreApiClaims;
import com.mycompany.api.basicapi.framework.oauth.CustomClaimsProvider;

/*
 * A factory interface for working around Java type erasure
 */
public interface ClaimsFactory<TClaims extends CoreApiClaims>  {

    // A new empty claims is created then populated on every request with a new token
    TClaims CreateEmptyClaims();

    // The claims cache is created at application startup
    ClaimsCache<TClaims> CreateClaimsCache();

    // A new custom claims provider is created then populated on every request with a new token
    CustomClaimsProvider<TClaims> CreateCustomClaimsProvider();
}
