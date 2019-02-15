package com.mycompany.api.basicapi.framework.oauth;

import com.mycompany.api.basicapi.framework.utilities.ClaimsFactory;

/*
 * A class to manage building and initializing our authorization filter
 */
public class AuthorizationFilterBuilder<TClaims extends CoreApiClaims> {

    // Our OAuth configuration
    private final OauthConfiguration configuration;

    // A utility object to work around Java type erasure
    private ClaimsFactory<TClaims> claimsFactory;

    // Trusted origins used with error responses
    private String[] trustedOrigins;

    /*
     * Receive configuration and initialize our data
     */
    public AuthorizationFilterBuilder(OauthConfiguration configuration) {
        this.configuration = configuration;
        this.claimsFactory = null;
        this.trustedOrigins = new String[] {};
    }

    /*
     * Set the factory used for creating objects
     */
    public AuthorizationFilterBuilder<TClaims> WithClaimsFactory(ClaimsFactory<TClaims> factory) {
        this.claimsFactory = factory;
        return this;
    }

    /*
     * Provide the type of custom claims provider
     */
    public AuthorizationFilterBuilder<TClaims> WithTrustedOrigins(String[] trustedOrigins) {
        this.trustedOrigins = trustedOrigins;
        return this;
    }

    /*
     * Build the authorization filter
     */
    public AuthorizationFilter<TClaims> Build() {

        // TODO: Validate and create defaults

        // Load metadata
        var metadata = new IssuerMetadata(this.configuration);
        metadata.initialize();

        // Create and initialize the claims cache
        var cache = this.claimsFactory.CreateClaimsCache();
        cache.initialize();

        // Create the filter
        return new AuthorizationFilter<TClaims>(
                this.configuration,
                metadata,
                cache,
                this.claimsFactory,
                this.trustedOrigins);
    }
}
