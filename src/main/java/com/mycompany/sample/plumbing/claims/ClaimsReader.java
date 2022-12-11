package com.mycompany.sample.plumbing.claims;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.sample.plumbing.errors.ErrorUtils;

/*
 * A utility to read claims values from objects
 */
public final class ClaimsReader {

    private ClaimsReader() {
    }

    /*
     * Return the base claims in a JWT that the API is interested in
     */
    public static BaseClaims baseClaims(final JwtClaims claimsSet) {

        var subject = ClaimsReader.getStringClaim(claimsSet, "sub");
        var scopes = ClaimsReader.getStringClaim(claimsSet, "scope").split(" ");
        var expiry = ClaimsReader.getExpiryClaim(claimsSet);
        return new BaseClaims(subject, scopes, expiry);
    }

    /*
     * Return the user info claims from a JWT
     */
    public static UserInfoClaims userInfoClaims(final JwtClaims claimsSet) {

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
     * Get a string claim from the claims payload
     */
    public static String getStringClaim(final JwtClaims data, final String name) {

        try {
            var value = data.getClaimValue(name, String.class);
            if (!StringUtils.hasLength(value)) {
                throw ErrorUtils.fromMissingClaim(name);
            }

            return value;

        } catch (MalformedClaimException ex) {
            throw ErrorUtils.fromMissingClaim(name);
        }
    }

    /*
     * Get a string array claim from the claims payload
     */
    public static String[] getStringArrayClaim(final JwtClaims data, final String name) {

        try {
            return data.getClaimValue(name, String[].class);

        } catch (MalformedClaimException ex) {
            throw ErrorUtils.fromMissingClaim(name);
        }
    }

    /*
     * Get an integer claim from the claims payload
     */
    private static int getExpiryClaim(final JwtClaims data) {

        try {
            return (int) data.getExpirationTime().getValue();

        } catch (MalformedClaimException ex) {
            throw ErrorUtils.fromMissingClaim("exp");
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
