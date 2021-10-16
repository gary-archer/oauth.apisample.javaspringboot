package com.mycompany.sample.host.claims;

import com.fasterxml.jackson.databind.JsonNode;
import com.mycompany.sample.logic.entities.SampleCustomClaims;
import com.mycompany.sample.plumbing.claims.BaseClaims;
import com.mycompany.sample.plumbing.claims.CustomClaims;
import com.mycompany.sample.plumbing.claims.CustomClaimsProvider;
import com.mycompany.sample.plumbing.claims.UserInfoClaims;

/*
 * A provider of domain specific claims
 */
public final class SampleCustomClaimsProvider extends CustomClaimsProvider {

    /*
     * When using the StandardAuthorizer this is called at the time of token issuance by the ClaimsController
     */
    public SampleCustomClaims issue(final String subject) {
        return (SampleCustomClaims) this.getCustomClaims(subject);
    }

    /*
     * When using the ClaimsCachingAuthorizer this is called when an API first receives the access token
     */
    public SampleCustomClaims get(
            final String accessToken,
            final BaseClaims baseClaims,
            final UserInfoClaims userInfo) {

        return (SampleCustomClaims) this.getCustomClaims(userInfo.getEmail());
    }

    /*
     * Ensure that custom claims are correctly deserialized
     */
    @Override
    protected CustomClaims deserialize(final JsonNode claimsNode) {
        return SampleCustomClaims.importData(claimsNode);
    }

    /*
     * Simulate some API logic for identifying the user from OAuth data, via either the subject or email claims
     * A real API would then do a database lookup to find the user's custom claims
     */
    private CustomClaims getCustomClaims(final String email) {

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
