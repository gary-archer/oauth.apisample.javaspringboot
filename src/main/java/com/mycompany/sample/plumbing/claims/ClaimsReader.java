package com.mycompany.sample.plumbing.claims;

import java.text.ParseException;
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.sample.plumbing.errors.ErrorUtils;
import com.nimbusds.jwt.JWTClaimsSet;

/*
 * A utility to read claims values from objects
 */
public final class ClaimsReader {

    private ClaimsReader() {
    }

    /*
     * Return the base claims in a JWT that the API is interested in
     */
    public static BaseClaims baseClaims(final JWTClaimsSet claimsSet) {

        var subject = ClaimsReader.getStringClaim(claimsSet, "sub");
        var scopes = ClaimsReader.getStringClaim(claimsSet, "scope").split(" ");
        var expiry = (int) claimsSet.getExpirationTime().toInstant().getEpochSecond();
        return new BaseClaims(subject, scopes, expiry);
    }

    /*
     * Return the user info claims from a JWT
     */
    public static UserInfoClaims userInfoClaims(final JWTClaimsSet claimsSet) {

        var givenName = ClaimsReader.getStringClaim(claimsSet, "given_name");
        var familyName = ClaimsReader.getStringClaim(claimsSet, "family_name");
        var email = ClaimsReader.getStringClaim(claimsSet, "email");
        return new UserInfoClaims(givenName, familyName, email);
    }

    /*
     * Return the user info claims from a User Info Lookup
     */
    public static UserInfoClaims userInfoClaims(final ObjectNode claims) {

        var givenName = ClaimsReader.getStringClaim(claims, "given_name");
        var familyName = ClaimsReader.getStringClaim(claims, "family_name");
        var email = ClaimsReader.getStringClaim(claims, "email");
        return new UserInfoClaims(givenName, familyName, email);
    }

    /*
     * Get a string claim from a claims set
     */
    private static String getStringClaim(final JWTClaimsSet data, final String name) {

        try {
            var value = data.getStringClaim(name);
            if (!StringUtils.hasLength(value)) {
                throw ErrorUtils.fromMissingClaim(name);
            }

            return value;

        } catch (ParseException ex) {

            throw new RuntimeException(String.format("Problem encountered parsing claim %s", name));
        }
    }

    /*
     * Get a string claim from a user info response
     */
    private static String getStringClaim(final ObjectNode data, final String name) {

        var claim = data.get(name).asText();
        if (!StringUtils.hasLength(name)) {
            throw ErrorUtils.fromMissingClaim(name);
        }

        return claim;
    }
}
