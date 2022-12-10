package com.mycompany.sample.plumbing.configuration;

import lombok.Getter;
import lombok.Setter;

/*
 * Configuration settings to enable standard security and extensible use of claims
 */
public class OAuthConfiguration {

    // The expected issuer in JWT access tokens received
    @Getter
    @Setter
    private String issuer;

    // The expected audience in JWT access tokens received
    @Getter
    @Setter
    private String audience;

    // The endpoint from which to download the token signing public key
    @Getter
    @Setter
    private String jwksEndpoint;

    // How to manage domain specific claims
    @Getter
    @Setter
    private String claimsStrategy;

    // Optional claims caching configuration
    @Getter
    @Setter
    private ClaimsCacheConfiguration claimsCache;
}
