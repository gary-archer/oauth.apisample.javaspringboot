package com.mycompany.sample.plumbing.oauth.tokenvalidation;

import com.nimbusds.jwt.JWTClaimsSet;

/*
 * An interface for validating tokens, which can have multiple implementations
 */
public interface TokenValidator {
    JWTClaimsSet validateToken(String accessToken);
}
