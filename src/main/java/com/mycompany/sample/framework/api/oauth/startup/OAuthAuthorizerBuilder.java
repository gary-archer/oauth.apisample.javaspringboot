package com.mycompany.sample.framework.api.oauth.startup;

import com.mycompany.sample.framework.api.base.security.CoreApiClaims;
import com.mycompany.sample.framework.api.oauth.claims.ClaimsSupplier;
import com.mycompany.sample.framework.api.oauth.claims.CustomClaimsProvider;
import com.mycompany.sample.framework.api.oauth.configuration.OAuthConfiguration;
import com.mycompany.sample.framework.api.oauth.security.IssuerMetadata;
import com.mycompany.sample.framework.api.oauth.security.OAuthAuthorizer;
import com.mycompany.sample.framework.api.base.logging.LoggerFactory;
import com.mycompany.sample.framework.api.oauth.claims.ClaimsCache;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.web.filter.OncePerRequestFilter;
import java.util.function.Supplier;

/*
 * Build an authorizer filter for OAuth token validation and claims caching
 */
public final class OAuthAuthorizerBuilder<TClaims extends CoreApiClaims> {

    // Injected properties
    private final ConfigurableListableBeanFactory container;
    private final OAuthConfiguration configuration;
    private final LoggerFactory loggerFactory;

    // Properties set by builder methods
    private Supplier<TClaims> claimsSupplier;
    private Supplier<CustomClaimsProvider<TClaims>> customClaimsProviderSupplier;

    /*
     * Receive configuration and initialize properties
     */
    public OAuthAuthorizerBuilder(
            final ConfigurableListableBeanFactory container,
            final OAuthConfiguration configuration,
            final LoggerFactory loggerFactory) {

        this.container = container;
        this.configuration = configuration;
        this.loggerFactory = loggerFactory;
        this.claimsSupplier = null;
        this.customClaimsProviderSupplier = null;
    }

    /*
     * Consumers of the framework must provide a callback for creating claims
     */
    public OAuthAuthorizerBuilder<TClaims> withClaimsSupplier(final Supplier<TClaims> claimsSupplier) {
        this.claimsSupplier = claimsSupplier;
        return this;
    }

    /*
     * Consumers of the framework can provide a callback for creating a custom claims provider
     */
    public OAuthAuthorizerBuilder<TClaims> withCustomClaimsProviderSupplier(
            final Supplier<CustomClaimsProvider<TClaims>> supplier) {

        this.customClaimsProviderSupplier = supplier;
        return this;
    }

    /*
     * Register and return the authorizer
     */
    public OncePerRequestFilter register() {

        // Create an injectable object to enable the framework to create claims objects of a concrete type at runtime
        var supplier = new ClaimsSupplier<TClaims>() {

            @Override
            public TClaims createEmptyClaims() {
                return claimsSupplier.get();
            }

            @Override
            public CustomClaimsProvider<TClaims> createCustomClaimsProvider() {
                return customClaimsProviderSupplier.get();
            }
        };

        // Load metadata if using OAuth security
        var metadata = new IssuerMetadata(this.configuration);
        metadata.initialize();

        // Create and initialize the claims cache
        var cache = new ClaimsCache<TClaims>(this.configuration, this.loggerFactory);
        cache.initialize();

        // Create the authorizer
        var authorizer = new OAuthAuthorizer(this.container);

        // Register framework specific dependencies
        this.registerSingletonDependencies(metadata, authorizer, cache, supplier);
        return authorizer;
    }

    /*
     * Register security dependencies at application startup
     */
    private void registerSingletonDependencies(
            final IssuerMetadata metadata,
            final OAuthAuthorizer authorizer,
            final ClaimsCache<TClaims> claims,
            final ClaimsSupplier<TClaims> supplier) {

        this.container.registerSingleton("OAuthConfiguration", this.configuration);
        this.container.registerSingleton("IssuerMetadata", metadata);
        this.container.registerSingleton("Authorizer", authorizer);
        this.container.registerSingleton("ClaimsCache", claims);
        this.container.registerSingleton("ClaimsSupplier", supplier);
    }
}
