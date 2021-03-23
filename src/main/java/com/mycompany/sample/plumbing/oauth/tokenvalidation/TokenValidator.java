package com.mycompany.sample.plumbing.oauth.tokenvalidation;

/*
 * An interface for validating tokens, which can have multiple implementations
 */
public interface TokenValidator {
    String validateToken(String accessToken);
}