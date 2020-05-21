package com.mycompany.sample.plumbing.dependencies;

import java.util.function.Supplier;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import com.mycompany.sample.plumbing.claims.ClaimsCache;
import com.mycompany.sample.plumbing.claims.ClaimsSupplier;
import com.mycompany.sample.plumbing.claims.CoreApiClaims;
import com.mycompany.sample.plumbing.claims.CustomClaimsProvider;
import com.mycompany.sample.plumbing.configuration.LoggingConfiguration;
import com.mycompany.sample.plumbing.configuration.OAuthConfiguration;
import com.mycompany.sample.plumbing.logging.LoggerFactory;
import com.mycompany.sample.plumbing.oauth.IssuerMetadata;
import com.mycompany.sample.plumbing.oauth.OAuthAuthorizer;
import com.mycompany.sample.plumbing.utilities.RequestClassifier;

/*
 * A class to manage composing core API behaviour
 */
public final class CompositionRoot<TClaims extends CoreApiClaims> {

    // The container to update
    private final ConfigurableListableBeanFactory container;

    // Injected properties
    private final LoggingConfiguration loggingConfiguration;
    private final OAuthConfiguration oauthConfiguration;
    private final LoggerFactory loggerFactory;

    // Properties set via builder methods
    private String apiBasePath;
    private Supplier<TClaims> claimsSupplier;
    private Supplier<CustomClaimsProvider<TClaims>> customClaimsProviderSupplier;

    /*
     * Receive configuration and initialize properties
     */
    public CompositionRoot(
            final ConfigurableListableBeanFactory container,
            final LoggingConfiguration loggingConfiguration,
            final OAuthConfiguration oauthConfiguration,
            final LoggerFactory loggerFactory) {

        this.container = container;
        this.loggingConfiguration = loggingConfiguration;
        this.oauthConfiguration = oauthConfiguration;
        this.loggerFactory = loggerFactory;
        this.claimsSupplier = null;
        this.customClaimsProviderSupplier = null;
    }

    /*
     * Record the API base path
     */
    public CompositionRoot withApiBasePath(final String apiBasePath) {

        this.apiBasePath = apiBasePath.toLowerCase();
        if (!this.apiBasePath.endsWith("/")) {
            this.apiBasePath += '/';
        }

        return this;
    }

    /*
     * Consumers must provide a callback for creating claims
     */
    public CompositionRoot<TClaims> withClaimsSupplier(final Supplier<TClaims> claimsSupplier) {
        this.claimsSupplier = claimsSupplier;
        return this;
    }

    /*
     * Consumers can provide a callback for creating a custom claims provider
     */
    public CompositionRoot<TClaims> withCustomClaimsProviderSupplier(
            final Supplier<CustomClaimsProvider<TClaims>> supplier) {

        this.customClaimsProviderSupplier = supplier;
        return this;
    }

    /*
     * Register and return the authorizer
     */
    public void register() {

        // Create an object used to prevent interceptors from processing SPA and OPTIONS requests
        var requestClassifier = new RequestClassifier(this.apiBasePath);

        // Create an injectable object to allow claims objects of a concrete type to be created at runtime
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
        var metadata = new IssuerMetadata(this.oauthConfiguration);
        metadata.initialize();

        // Create and initialize the claims cache
        var cache = new ClaimsCache<TClaims>(this.oauthConfiguration, this.loggerFactory);
        cache.initialize();

        // Create the authorizer, which is a Spring once per request filter
        var authorizer = new OAuthAuthorizer(this.container);

        // Register these singletons so that they are injectable
        this.registerSingletonDependencies(
                requestClassifier,
                metadata,
                authorizer,
                cache,
                supplier);
    }

    /*
     * Register dependencies at application startup
     */
    private void registerSingletonDependencies(
            final RequestClassifier requestClassifier,
            final IssuerMetadata metadata,
            final OAuthAuthorizer authorizer,
            final ClaimsCache<TClaims> claims,
            final ClaimsSupplier<TClaims> supplier) {

        // Configuration objects
        this.container.registerSingleton("LoggingConfiguration", this.loggingConfiguration);
        this.container.registerSingleton("OAuthConfiguration", this.oauthConfiguration);

        // Logging and REST objects
        this.container.registerSingleton("LoggerFactory", this.loggerFactory);
        this.container.registerSingleton("RequestClassifier", requestClassifier);

        // OAuth objects
        this.container.registerSingleton("IssuerMetadata", metadata);
        this.container.registerSingleton("Authorizer", authorizer);
        this.container.registerSingleton("ClaimsCache", claims);
        this.container.registerSingleton("ClaimsSupplier", supplier);
    }
}
