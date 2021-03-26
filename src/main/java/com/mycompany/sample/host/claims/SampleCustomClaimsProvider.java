package com.mycompany.sample.host.claims;

import com.fasterxml.jackson.databind.JsonNode;
import com.mycompany.sample.logic.entities.SampleCustomClaims;
import com.mycompany.sample.plumbing.claims.ClaimsPayload;
import com.mycompany.sample.plumbing.claims.CustomClaims;
import com.mycompany.sample.plumbing.claims.CustomClaimsProvider;

/*
 * An example of including domain specific details in cached claims
 */
public final class SampleCustomClaimsProvider extends CustomClaimsProvider {

    /*
     * When using the StandardAuthorizer this is called at the time of token issuance by the ClaimsController
     * My Authorization Server setup currently sends the user's email as the subject claim
     */
    public SampleCustomClaims supplyCustomClaimsFromSubject(final String subject) {
        return (SampleCustomClaims) this.supplyCustomClaims(subject);
    }

    /*
     * When using the ClaimsCachingAuthorizer this is called when the API first receives the access token
     */
    @Override
    protected CustomClaims supplyCustomClaims(
            final ClaimsPayload tokenData,
            final ClaimsPayload userInfoData) {

        return this.supplyCustomClaims(userInfoData.getStringClaim("email"));
    }

    /*
     * When using the StandardAuthorizer we read all custom claims directly from the token
     */
    @Override
    protected CustomClaims readCustomClaims(final ClaimsPayload token) {

        var userId = token.getStringClaim("user_id");
        var userRole = token.getStringClaim("user_role");
        var userRegions = token.getStringClaim("user_regions").split(" ");
        return new SampleCustomClaims(userId, userRole, userRegions);
    }

    /*
     * Ensure that custom claims are correctly deserialized
     */
    @Override
    protected CustomClaims deserializeCustomClaimsFromCache(final JsonNode claimsNode) {
        return SampleCustomClaims.importData(claimsNode);
    }

    /*
     * Simulate some API logic for identifying the user from OAuth data, via either the subject or email claims
     * A real API would then do a database lookup to find the user's custom claims
     */
    private CustomClaims supplyCustomClaims(final String email) {

        var isAdmin = email.toLowerCase().contains("admin");
        if (isAdmin) {

            // For admin users we hard code this user id, assign a role of 'admin' and grant access to all regions
            // The CompanyService class will use these claims to return all transaction data
            return new SampleCustomClaims("20116", "admin", new String[]{});

        } else {

            // For other users we hard code this user id, assign a role of 'user' and grant access to only one region
            // The CompanyService class will use these claims to return only transactions for the US region
            return new SampleCustomClaims("10345", "user", new String[]{"USA"});
        }
    }
}
