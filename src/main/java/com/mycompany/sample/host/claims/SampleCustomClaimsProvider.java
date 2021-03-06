package com.mycompany.sample.host.claims;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.sample.logic.entities.SampleCustomClaims;
import com.mycompany.sample.plumbing.claims.CustomClaims;
import com.mycompany.sample.plumbing.claims.CustomClaimsProvider;
import com.mycompany.sample.plumbing.claims.TokenClaims;
import com.mycompany.sample.plumbing.claims.UserInfoClaims;

/*
 * An example of including domain specific details in cached claims
 */
public final class SampleCustomClaimsProvider extends CustomClaimsProvider {

    /*
     * An example of how custom claims can be included
     */
    @Override
    public CustomClaims getCustomClaims(final TokenClaims token, final UserInfoClaims userInfo) {

        // A real implementation would look up the database user id from the subject and / or email claim
        var email = userInfo.getEmail();
        var userDatabaseId = "10345";

        // Our blog's code samples have two fixed users and we use the below mock implementation:
        // - guestadmin@mycompany.com is an admin and sees all data
        // - guestuser@mycompany.com is not an admin and only sees data for the USA region
        var isAdmin = email.toLowerCase().contains("admin");
        var regionsCovered = isAdmin ? new String[]{} : new String[]{"USA"};

        return new SampleCustomClaims(userDatabaseId, isAdmin, regionsCovered);
    }

    /*
     * Ensure that custom claims are correctly deserialized
     */
    @Override
    protected CustomClaims deserializeCustomClaims(final ObjectNode claimsNode) {
        return SampleCustomClaims.importData(claimsNode);
    }
}
