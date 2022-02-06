package com.mycompany.sample.host.claims;

import org.jose4j.jwt.JwtClaims;
import com.fasterxml.jackson.databind.JsonNode;
import com.mycompany.sample.logic.entities.SampleCustomClaims;
import com.mycompany.sample.plumbing.claims.BaseClaims;
import com.mycompany.sample.plumbing.claims.ClaimsReader;
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
    @Override
    public CustomClaims issue(final String subject) {
        return this.getCustomClaims(subject);
    }

    /*
     * When using the StandardAuthorizer this is called to read custom claims from the JWT
     */
    @Override
    public CustomClaims get(final JwtClaims payload) {

        var userId = ClaimsReader.getStringClaim(payload, "user_id");
        var userRole = ClaimsReader.getStringClaim(payload, "user_role");
        var userRegions = ClaimsReader.getStringArrayClaim(payload, "user_role");
        return new SampleCustomClaims(userId, userRole, userRegions);
    }

    /*
     * When using the ClaimsCachingAuthorizer this is called when an API first receives the access token
     */
    @Override
    public SampleCustomClaims get(
            final String accessToken,
            final BaseClaims baseClaims,
            final UserInfoClaims userInfo) {

        return (SampleCustomClaims) this.getCustomClaims(baseClaims.getSubject());
    }

    /*
     * When using the ClaimsCaching authorizer this manages deserialization from the cache
     */
    @Override
    public CustomClaims deserialize(final JsonNode claimsNode) {
        return SampleCustomClaims.importData(claimsNode);
    }

    /*
     * Determine the user in business terms from the Authorization Server's subject claim
     */
    private CustomClaims getCustomClaims(final String subject) {

        var isAdmin = subject.equals("77a97e5b-b748-45e5-bb6f-658e85b2df91");
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
