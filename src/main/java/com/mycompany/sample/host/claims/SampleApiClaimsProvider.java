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
     * A simple example of applying domain specific claims
     */
    private void lookupAuthorizationData(final SampleApiClaims claims) {

        // Our blog's code samples have two fixed users and use the below mock implementation:
        // - guestadmin@mycompany.com is an admin and sees all data
        // - guestuser@mycompany.com is not an admin and only sees data for their own region
        claims.setAdmin(claims.getEmail().toLowerCase().contains("admin"));

        // We use a coverage based authorization rule where the user can only use data for these regions
        claims.setRegionsCovered(new String[]{"USA"});
    }
}
