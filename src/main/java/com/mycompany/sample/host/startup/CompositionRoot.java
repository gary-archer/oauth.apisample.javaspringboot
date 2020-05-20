package com.mycompany.sample.host.startup;

import com.mycompany.sample.host.plumbing.claims.CoreApiClaims;
import com.mycompany.sample.host.plumbing.claims.ClaimsSupplier;
import com.mycompany.sample.host.plumbing.claims.CustomClaimsProvider;
import com.mycompany.sample.host.configuration.Configuration;
import com.mycompany.sample.host.plumbing.oauth.IssuerMetadata;
import com.mycompany.sample.host.plumbing.oauth.OAuthAuthorizer;
import com.mycompany.sample.host.plumbing.logging.LoggerFactory;
import com.mycompany.sample.host.plumbing.claims.ClaimsCache;
import com.mycompany.sample.host.plumbing.utilities.RequestClassifier;
import java.util.function.Supplier;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/*
 * A class to manage composing core API behaviour
 */
public final class CompositionRoot<TClaims extends CoreApiClaims> {

    // Properties to be registered for injection
    private final ConfigurableListableBeanFactory container;
    private final Configuration configuration;
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
            final Configuration configuration,
            final LoggerFactory loggerFactory) {

        this.container = container;
        this.configuration = configuration;
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
        var metadata = new IssuerMetadata(this.configuration.getOauth());
        metadata.initialize();

        // Create and initialize the claims cache
        var cache = new ClaimsCache<TClaims>(this.configuration.getOauth(), this.loggerFactory);
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
        this.container.registerSingleton("ApiConfiguration", this.configuration.getApi());
        this.container.registerSingleton("OAuthConfiguration", this.configuration.getOauth());
        this.container.registerSingleton("LoggingConfiguration", this.configuration.getLogging());

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
