package com.mycompany.sample.logic.claims;

import com.fasterxml.jackson.databind.JsonNode;
import com.mycompany.sample.logic.entities.SampleCustomClaims;
import com.mycompany.sample.plumbing.claims.BaseClaims;
import com.mycompany.sample.plumbing.claims.CustomClaims;
import com.mycompany.sample.plumbing.claims.CustomClaimsProvider;

/*
 * A provider of custom claims from the business data
 */
public final class SampleCustomClaimsProvider extends CustomClaimsProvider {

    /*
     * Look up custom claims when details are not available in the cache, such as for a new access token
     */
    @Override
    public CustomClaims lookupForNewAccessToken(final String accessToken, final BaseClaims baseClaims) {

        var isAdmin = baseClaims.getSubject().equals("77a97e5b-b748-45e5-bb6f-658e85b2df91");
        if (isAdmin) {

            // For admin users we hard code this user id, assign a role of 'admin' and grant access to all regions
            // The CompanyService class will use these claims to return all transaction data
            return new SampleCustomClaims("20116", "admin", new String[]{"Europe", "USA", "Asia"});

        } else {

            // For other users we hard code this user id, assign a role of 'user' and grant access to only one region
            // The CompanyService class will use these claims to return only transactions for the US region
            return new SampleCustomClaims("10345", "user", new String[]{"USA"});
        }
    }

    /*
     * When custom claims are in the cache, deserialize them into an object
     */
    @Override
    public CustomClaims deserializeFromCache(final JsonNode claimsNode) {
        return SampleCustomClaims.importData(claimsNode);
    }
}
