package com.mycompany.sample.plumbing.configuration;

import lombok.Getter;
import lombok.Setter;

/*
 * Configuration settings to enable extensible use of claims
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

    // The maximum number of minutes for which to cache claims
    @Getter
    @Setter
    private int claimsCacheTimeToLiveMinutes;
}
