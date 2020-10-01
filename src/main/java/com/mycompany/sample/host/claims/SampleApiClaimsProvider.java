package com.mycompany.sample.host.claims;

import javax.servlet.http.HttpServletRequest;
import com.mycompany.sample.plumbing.claims.CustomClaimsProvider;

/*
 * An example of including domain specific details in cached claims
 */
public final class SampleApiClaimsProvider extends CustomClaimsProvider<SampleApiClaims> {

    /*
     * Add details from the API's own database
     */
    @Override
    public void addCustomClaims(
            final String accessToken,
            final HttpServletRequest request,
            final SampleApiClaims claims) {

        // Look up the user id in the API's own database
        this.lookupDatabaseUserId(claims);

        // Look up the user id in the API's own data
        this.lookupAuthorizationData(claims);
    }

    /*
     * A real implementation would get the subject / email claims and find a match in the API's own data
     */
    private void lookupDatabaseUserId(final SampleApiClaims claims) {
        claims.setUserDatabaseId("10345");
    }

    /*
     * A real implementation would look up authorization data from the API's own data
     * This could include user roles and any data used for enforcing authorization rules
     */
    private void lookupAuthorizationData(final SampleApiClaims claims) {

        // We use a coverage based authorization rule where the user can only use data for these regions
        claims.setRegionsCovered(new String[]{"Europe", "USA"});
    }
}
