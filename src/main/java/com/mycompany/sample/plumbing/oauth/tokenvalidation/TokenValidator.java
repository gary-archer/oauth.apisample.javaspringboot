package com.mycompany.sample.plumbing.oauth.tokenvalidation;

import com.mycompany.sample.plumbing.claims.ClaimsPayload;

/*
 * An interface for validating tokens, which can have multiple implementations
 */
public interface TokenValidator {
    ClaimsPayload validateToken(String accessToken);
}
