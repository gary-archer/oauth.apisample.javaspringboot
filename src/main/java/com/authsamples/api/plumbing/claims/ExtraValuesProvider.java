package com.authsamples.api.plumbing.claims;

import org.jose4j.jwt.JwtClaims;

/*
 * An interface through which OAuth plumbing code calls a repository in the API logic
 */
public interface ExtraValuesProvider {
    ExtraValues lookupExtraValues(JwtClaims jwtClaims);
}
