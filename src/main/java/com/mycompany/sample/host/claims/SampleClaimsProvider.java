package com.mycompany.sample.host.claims;

import com.fasterxml.jackson.databind.JsonNode;
import com.mycompany.sample.logic.entities.SampleCustomClaims;
import com.mycompany.sample.plumbing.claims.ClaimParser;
import com.mycompany.sample.plumbing.claims.ClaimsProvider;
import com.mycompany.sample.plumbing.claims.CustomClaims;
import com.nimbusds.jwt.JWTClaimsSet;

/*
 * A class to provide claims for the sample API
 */
public final class SampleClaimsProvider extends ClaimsProvider {

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
            final JWTClaimsSet tokenData,
            final JWTClaimsSet userInfoData) {

        var email = ClaimParser.getStringClaim(userInfoData, "email");
        return this.supplyCustomClaims(email);
    }

    /*
     * When using the StandardAuthorizer we read all custom claims directly from the token
     */
    @Override
    protected CustomClaims readCustomClaims(final JWTClaimsSet claimsSet) {
        return new SampleCustomClaims(claimsSet);
    }

    /*
     * Ensure that custom claims are correctly deserialized
     */
    @Override
    protected CustomClaims deserializeCustomClaims(final JsonNode claimsNode) {
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
