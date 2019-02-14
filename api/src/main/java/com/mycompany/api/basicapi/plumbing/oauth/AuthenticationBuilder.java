package com.mycompany.api.basicapi.plumbing.oauth;

/*
 * A class to manage building and initializing our authorization filter
 */
public class AuthenticationBuilder<TClaims extends CoreApiClaims> {

    // Our OAuth configuration
    private final OauthConfiguration configuration;

    // The type of custom claims provider
    private Class customClaimsProviderType;

    /*
     * Create our builder and receive configuration
     */
    public AuthenticationBuilder(OauthConfiguration configuration) {
        this.configuration = configuration;
    }

    /*
     * Provide the type of custom claims provider
     */
    public <TProvider> AuthenticationBuilder<TClaims> WithCustomClaimsProvider(Class<TProvider> runtimeType) {
        this.customClaimsProviderType = runtimeType;
        return this;
    }
}
