package com.authsamples.api.plumbing.claims;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.springframework.util.StringUtils;
import com.authsamples.api.plumbing.errors.ErrorUtils;

/*
 * A utility to read claims defensively
 */
public final class ClaimsReader {

    private ClaimsReader() {
    }

    /*
     * Get a mandatory string claim from the claims payload
     */
    public static String getStringClaim(final JwtClaims data, final String name) {
        return ClaimsReader.getStringClaim(data, name, true);
    }

    /*
     * Get a string claim from the claims payload
     */
    public static String getStringClaim(final JwtClaims data, final String name, final boolean required) {

        try {
            var value = data.getClaimValue(name, String.class);
            if (!StringUtils.hasLength(value)) {
                if (required) {
                    throw ErrorUtils.fromMissingClaim(name);
                } else {
                    return "";
                }
            }

            return value;

        } catch (MalformedClaimException ex) {
            throw ErrorUtils.fromMissingClaim(name);
        }
    }

    /*
     * Get an integer claim from the claims payload
     */
    public static int getExpiryClaim(final JwtClaims data) {

        try {
            return (int) data.getExpirationTime().getValue();

        } catch (MalformedClaimException ex) {
            throw ErrorUtils.fromMissingClaim("exp");
        }
    }
}
