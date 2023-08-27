package com.mycompany.sample.logic.claims;

import org.jose4j.jwt.JwtClaims;
import com.fasterxml.jackson.databind.JsonNode;
import com.mycompany.sample.plumbing.claims.ClaimsPrincipal;
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
    public ExtraClaims lookupExtraClaims(final JwtClaims jwtClaims) {

        // First get values from the token, or look them up when they are not received
        boolean lookedUp = false;
        String managerId = null;
        String role = null;
        if (jwtClaims.hasClaim("manager_id") && jwtClaims.hasClaim("role")) {

            managerId = ClaimsReader.getStringClaim(jwtClaims, "manager_id");
            role = ClaimsReader.getStringClaim(jwtClaims, "role");

        } else {

            lookedUp = true;
            var subject = ClaimsReader.getStringClaim(jwtClaims, "sub");
            if (subject.equals("77a97e5b-b748-45e5-bb6f-658e85b2df91")) {
                managerId = "20116";
                role = "admin";
            } else {
                managerId = "10345";
                role = "user";
            }
        }

        // A real API would use a database, but this API uses a mock implementation
        if (managerId.equals("20116")) {

            // These claims are used for the guestadmin@mycompany.com user account
            var extraClaims = new SampleExtraClaims("Global Manager", new String[]{"Europe", "USA", "Asia"});
            if (lookedUp) {
                extraClaims.addTokenClaims(managerId, role);
            }
            return extraClaims;

        } else {

            // These claims are used for the guestuser@mycompany.com user account
            var extraClaims = new SampleExtraClaims("Regional Manager", new String[]{"USA"});
            if (lookedUp) {
                extraClaims.addTokenClaims(managerId, role);
            }
            return extraClaims;
        }
    }

    /*
     * Return a claims principal with some claims lookup workarounds
     */
    @Override
    public ClaimsPrincipal createClaimsPrincipal(final JwtClaims jwtClaims, final ExtraClaims extraClaims) {
        return new SampleClaimsPrincipal(jwtClaims, extraClaims);
    }

    /*
     * Deserialize extra claims after they have been read from the cache
     */
    @Override
    public ExtraClaims deserializeFromCache(final JsonNode claimsNode) {
        return SampleExtraClaims.importData(claimsNode);
    }
}
