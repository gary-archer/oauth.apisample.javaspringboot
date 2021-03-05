package com.mycompany.sample.plumbing.claims;

/*
 * This class is injected into authentication handling
 */
@SuppressWarnings("PMD.GenericsNaming")
public interface ClaimsSupplier<TClaims extends ApiClaims>  {

    // A new empty claims is created then populated on every request with a new token
    TClaims createEmptyClaims();

    // A new custom claims provider is created then populated on every request with a new token
    CustomClaimsProvider<TClaims> createCustomClaimsProvider();
}
