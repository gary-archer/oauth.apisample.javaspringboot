package com.mycompany.sample.logic.claims;

import org.jose4j.jwt.JwtClaims;
import com.fasterxml.jackson.databind.JsonNode;
import com.mycompany.sample.logic.entities.SampleCustomClaims;
import com.mycompany.sample.plumbing.claims.ClaimsReader;
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
    public CustomClaims lookupForNewAccessToken(final String accessToken, final JwtClaims jwtClaims) {

        // It is common to need to get a business user ID for the authenticated user
        // In our example a manager user may be able to view information about investors
        var managerId = this.getManagerId(jwtClaims);

        // A real API would use a database, but this API uses a mock implementation
        if (managerId.equals("20116")) {

            // These custom claims are used for the guestadmin@mycompany.com user account
            return new SampleCustomClaims(managerId, "admin", new String[]{"Europe", "USA", "Asia"});

        } else {

            // These custom claims are used for the guestuser@mycompany.com user account
            return new SampleCustomClaims(managerId, "user", new String[]{"USA"});
        }
    }

    /*
     * When custom claims are in the cache, deserialize them into an object
     */
    @Override
    public CustomClaims deserializeFromCache(final JsonNode claimsNode) {
        return SampleCustomClaims.importData(claimsNode);
    }

    /*
     * Get a business user ID that corresponds to the user in the token
     */
    private String getManagerId(final JwtClaims jwtClaims) {

        if (jwtClaims.hasClaim("manager_id")) {

            // The preferred option is for the API to receive the business user identity in the JWT access token
            return ClaimsReader.getStringClaim(jwtClaims, "manager_id");

        } else {

            // Otherwise the API must determine the value from the subject claim
            var subject = ClaimsReader.getStringClaim(jwtClaims, "sub");
            return this.lookupManagerIdFromSubjectClaim(subject);
        }
    }

    /*
     * The API could store a mapping from the subject claim to the business user identity
     */
    private String lookupManagerIdFromSubjectClaim(final String subject) {

        // A real API would use a database, but this API uses a mock implementation
        // This subject value is for the guestadmin@mycompany.com user account
        var isAdmin = subject.equals("77a97e5b-b748-45e5-bb6f-658e85b2df91");
        if (isAdmin) {
            return "20116";
        } else {
            return "10345";
        }
    }
}
