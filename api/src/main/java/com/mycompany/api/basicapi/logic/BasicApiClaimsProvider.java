package com.mycompany.api.basicapi.logic;

import com.mycompany.api.basicapi.entities.BasicApiClaims;
import com.mycompany.api.basicapi.plumbing.oauth.CustomClaimsProvider;

/*
 * Extend our base class to provide custom claims
 */
public class BasicApiClaimsProvider extends CustomClaimsProvider<BasicApiClaims> {

    /*
     * The interface supports returning results based on the user id from the token
     * This might involve a database lookup or a call to another service
     */
    @Override
    public void addCustomClaims(String accessToken, BasicApiClaims claims) {

        // Any attempts to access data for company 3 will result in an unauthorized error
        var accountsCovered = new int[]{1, 2, 4};
        claims.setAccountsCovered(accountsCovered);
    }
}
