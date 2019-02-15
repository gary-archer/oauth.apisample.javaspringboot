package com.mycompany.api.basicapi.framework.oauth;

import java.util.function.Supplier;

/*
 * A class to manage building and initializing our authorization filter
 */
public class AuthorizationFilterBuilder<TClaims extends CoreApiClaims> {

    // Our OAuth configuration
    private final OauthConfiguration configuration;

    // The issuer metadata
    private IssuerMetadata metadata;

    // The claims cache
    private ClaimsCache<TClaims> cache;

    // An object to create a new empty claims object when required
    private Supplier<TClaims> claimsSupplier;

    // The custom claims provider
    private Supplier<CustomClaimsProvider<TClaims>> customClaimsProvider;

    // Trusted origins used with error responses
    private String[] trustedOrigins;

    /*
     * Receive configuration and initialize our data
     */
    public AuthorizationFilterBuilder(OauthConfiguration configuration) {
        this.configuration = configuration;
        this.metadata = null;
        this.cache = null;
        this.claimsSupplier = null;
        this.customClaimsProvider = null;
        this.trustedOrigins = new String[] {};
    }

    /*
     * Set the metadata
     */
    public AuthorizationFilterBuilder<TClaims> WithIssuerMetadata(IssuerMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

    /*
     * Set the cache
     */
    public AuthorizationFilterBuilder<TClaims> WithClaimsCache(ClaimsCache<TClaims> cache) {
        this.cache = cache;
        return this;
    }

    /*
     * Set the claims supplier
     */
    public AuthorizationFilterBuilder<TClaims> WithClaimsSupplier(Supplier<TClaims> claimsSupplier) {
        this.claimsSupplier = claimsSupplier;
        return this;
    }

    /*
     * Set an object to create the type of custom claims provider needed
     */
    public <TProvider extends Supplier<CustomClaimsProvider<TClaims>>>
           AuthorizationFilterBuilder<TClaims> WithCustomClaimsProvider(TProvider provider) {

        this.customClaimsProvider = provider;
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

        // Create the filter
        return new AuthorizationFilter<TClaims>(
                this.configuration,
                this.metadata,
                this.cache,
                this.claimsSupplier,
                this.customClaimsProvider,
                this.trustedOrigins);
    }
}
