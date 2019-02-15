package com.mycompany.api.basicapi.framework.oauth;

/*
 * The base class for providing custom claims
 */
public class CustomClaimsProvider<TClaims extends CoreApiClaims> {

    /*
     * This can be overridden by derived classes to add custom claims
     */
    public void addCustomClaims(String accessToken, TClaims claims)
    {
    }
}