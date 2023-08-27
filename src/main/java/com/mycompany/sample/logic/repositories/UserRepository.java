package com.mycompany.sample.logic.repositories;

import com.mycompany.sample.logic.claims.SampleExtraClaims;

/*
 * A repository that returns hard coded data, whereas a real implementation would use a database lookup
 */
public final class UserRepository {

    /*
     * Receive the manager ID in the access token, as a useful API identity, then look up extra claims
     * This is the preferred model, since it locks down the access token and provides the most useful API user identity
     */
    public SampleExtraClaims getClaimsForManagerId(final String managerId) {

        if (managerId.equals("20116")) {

            // These claims are used for the guestadmin@mycompany.com user account
            return new SampleExtraClaims("Global Manager", new String[]{"Europe", "USA", "Asia"});

        } else {

            // These claims are used for the guestuser@mycompany.com user account
            return new SampleExtraClaims("Regional Manager", new String[]{"USA"});
        }
    }

    /*
     * Receive the subject claim from the access token and look up all other claims
     * This is less optimal, since the token is less locked down and the API must map the subject to other values
     */
    public SampleExtraClaims getClaimsForSubject(final String subject) {

        if (subject.equals("77a97e5b-b748-45e5-bb6f-658e85b2df91")) {

            // These claims are used for the guestadmin@mycompany.com user account
            System.out.println("USING ADMIN");
            var claims = new SampleExtraClaims("Global Manager", new String[]{"Europe", "USA", "Asia"});
            claims.addMainClaims("20116", "admin");
            return claims;

        } else {

            // These claims are used for the guestuser@mycompany.com user account
            System.out.println("USING USER");
            var claims = new SampleExtraClaims("Regional Manager", new String[]{"USA"});
            claims.addMainClaims("10345", "user");
            return claims;
        }
    }
}
