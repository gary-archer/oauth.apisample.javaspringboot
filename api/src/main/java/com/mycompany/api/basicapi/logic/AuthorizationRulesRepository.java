package com.mycompany.api.basicapi.logic;

import com.mycompany.api.basicapi.entities.BasicApiClaims;
import com.mycompany.api.basicapi.plumbing.oauth.CoreApiClaims;
import com.mycompany.api.basicapi.plumbing.oauth.CustomClaimsRepository;

/*
 * A stub class for returning domain specific authorization rules
 */
public class AuthorizationRulesRepository implements CustomClaimsRepository {

    /*
     * The interface supports returning results based on the user id from the token
     * This might involve a database lookup or a call to another service
     */
    public void addCustomClaims(String accessToken, CoreApiClaims claims) {

        // Any attempts to access data for company 3 will result in an unauthorized error
        var accountsCovered = new int[]{1, 2, 4};
        ((BasicApiClaims)claims).setAccountsCovered(accountsCovered);
    }
}
