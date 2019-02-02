package com.mycompany.api.basicapi.plumbing.oauth;

/*
 * An interface for adding custom claims from within core claims handling code
 */
public interface CustomClaimsRepository {

    void addCustomClaims(String accessToken, CoreApiClaims claims);
}
