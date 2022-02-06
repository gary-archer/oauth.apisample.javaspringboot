package com.mycompany.sample.plumbing.configuration;

import lombok.Getter;
import lombok.Setter;

/*
 * OAuth configuration settings
 */
public class OAuthConfiguration {

    // Certain behaviour may be triggered by a provider's capabilities
    @Getter
    @Setter
    private String provider;

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

    // The URL to the Authorization Server's user info endpoint, if needed
    @Getter
    @Setter
    private String userInfoEndpoint;

    // The maximum number of minutes for which to cache claims, when applicable
    @Getter
    @Setter
    private int claimsCacheTimeToLiveMinutes;
}
