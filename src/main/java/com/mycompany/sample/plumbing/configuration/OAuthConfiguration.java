package com.mycompany.sample.plumbing.configuration;

import lombok.Getter;
import lombok.Setter;

/*
 * OAuth configuration settings
 */
public class OAuthConfiguration {

    // The OAuth strategy to use, either 'standard' or 'claims-caching'
    @Getter
    @Setter
    private String strategy;

    // The expected issuer of access tokens
    @Getter
    @Setter
    private String issuer;

    // The expected audience of access tokens
    @Getter
    @Setter
    private String audience;

    // The strategy for validating access tokens, either 'jwt' or 'introspection'
    @Getter
    @Setter
    private String tokenValidationStrategy;

    // The endpoint from which to download the token signing public key, when validating JWTs
    @Getter
    @Setter
    private String jwksEndpoint;

    // The endpoint for token introspection
    @Getter
    @Setter
    private String introspectEndpoint;

    // The client id with which to call the introspection endpoint
    @Getter
    @Setter
    private String introspectClientId;

    // The client secret with which to call the introspection endpoint
    @Getter
    @Setter
    private String introspectClientSecret;

    // The URL to the Authorization Server's user info endpoint, which could be an internal URL
    // This is used with the claims caching strategy, when we need to look up user info claims
    @Getter
    @Setter
    private String userInfoEndpoint;

    // The maximum number of minutes for which to cache claims, when using the claims caching strategy
    @Getter
    @Setter
    private int claimsCacheTimeToLiveMinutes;
}
