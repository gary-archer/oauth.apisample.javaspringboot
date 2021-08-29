package com.mycompany.sample.plumbing.claims;

import java.text.ParseException;
import org.springframework.util.StringUtils;
import com.mycompany.sample.plumbing.errors.ErrorUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.oauth2.sdk.TokenIntrospectionSuccessResponse;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;

/*
 * Utility methods when reading claims values and dealing with parse exceptions or missing data
 */
public final class ClaimParser {

    private ClaimParser() {
    }

    /*
     * Get a string claim from a claims set
     */
    public static String getStringClaim(final JWTClaimsSet data, final String name) {

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
     * Get a string claim from an introspection response
     */
    public static String getStringClaim(final TokenIntrospectionSuccessResponse data, final String name) {

        var claim = data.getStringParameter(name);
        if (claim == null) {
            throw ErrorUtils.fromMissingClaim(name);
        }

        return claim;
    }

    /*
     * Get a string claim from a user info response
     */
    public static String getStringClaim(final UserInfo data, final String name) {

        var claim = data.getStringClaim(name);
        if (!StringUtils.hasLength(name)) {
            throw ErrorUtils.fromMissingClaim(name);
        }

        return claim;
    }

    /*
     * Get a string array claim and handle parse exceptions
     */
    public static String[] getStringArrayClaim(final JWTClaimsSet data, final String name) {

        try {
            return data.getStringArrayClaim(name);

        } catch (ParseException ex) {

            throw new RuntimeException(String.format("Problem encountered parsing claim %s", name));
        }
    }
}
