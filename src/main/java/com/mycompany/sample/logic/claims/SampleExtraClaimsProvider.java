package com.mycompany.sample.logic.claims;

import org.jose4j.jwt.JwtClaims;
import com.fasterxml.jackson.databind.JsonNode;
import com.mycompany.sample.logic.entities.SampleExtraClaims;
import com.mycompany.sample.plumbing.claims.ClaimsReader;
import com.mycompany.sample.plumbing.claims.ExtraClaims;
import com.mycompany.sample.plumbing.claims.ExtraClaimsProvider;

/*
 * Add extra claims that you cannot, or do not want to, manage in the authorization server
 */
public final class SampleExtraClaimsProvider extends ExtraClaimsProvider {

    /*
     * Get additional claims from the API's own database
     */
    @Override
    public ExtraClaims lookupBusinessClaims(final String accessToken, final JwtClaims jwtClaims) {

        // It is common to need to get a business user ID for the authenticated user
        // In our example a manager user may be able to view information about investors
        var managerId = this.getManagerId(jwtClaims);

        // A real API would use a database, but this API uses a mock implementation
        if (managerId.equals("20116")) {

            // These claims are used for the guestadmin@mycompany.com user account
            return new SampleExtraClaims(managerId, "admin", new String[]{"Europe", "USA", "Asia"});

        } else {

            // These claims are used for the guestuser@mycompany.com user account
            return new SampleExtraClaims(managerId, "user", new String[]{"USA"});
        }
    }

    /*
     * Deserialize extra claims after they have been read from the cache
     */
    @Override
    public ExtraClaims deserializeFromCache(final JsonNode claimsNode) {
        return SampleExtraClaims.importData(claimsNode);
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
