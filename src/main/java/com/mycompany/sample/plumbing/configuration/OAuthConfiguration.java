package com.mycompany.sample.plumbing.configuration;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.mycompany.sample.plumbing.utilities.UnderscorePropertyNamingStrategy;
import lombok.Getter;
import lombok.Setter;

/*
 * OAuth configuration settings
 */
@JsonNaming(UnderscorePropertyNamingStrategy.class)
public class OAuthConfiguration {

    // The OAuth strategy to use, either 'standard' or 'claims-caching'
    @Getter
    @Setter
    private String _strategy;

    // The expected issuer of access tokens
    @Getter
    @Setter
    private String _issuer;

    // The expected audience of access tokens
    @Getter
    @Setter
    private String _audience;

    // The strategy for validating access tokens, either 'jwt' or 'introspection'
    @Getter
    @Setter
    private String _tokenValidationStrategy;

    // The endpoint from which to download the token signing public key, when validating JWTs
    @Getter
    @Setter
    private String _jwksEndpoint;

    // The endpoint for token introspection
    @Getter
    @Setter
    private String _introspectEndpoint;

    // The client id with which to call the introspection endpoint
    @Getter
    @Setter
    private String _introspectClientId;

    // The client secret with which to call the introspection endpoint
    @Getter
    @Setter
    private String _introspectClientSecret;

    // The URL to the Authorization Server's user info endpoint, which could be an internal URL
    // This is used with the claims caching strategy, when we need to look up user info claims
    @Getter
    @Setter
    private String _userInfoEndpoint;

    // The maximum number of minutes for which to cache claims, when using the claims caching strategy
    @Getter
    @Setter
    private int _claimsCacheTimeToLiveMinutes;
}
