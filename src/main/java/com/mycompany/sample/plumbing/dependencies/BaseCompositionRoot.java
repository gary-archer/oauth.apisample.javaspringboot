package com.mycompany.sample.plumbing.dependencies;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import com.mycompany.sample.plumbing.claims.ClaimsCache;
import com.mycompany.sample.plumbing.claims.CustomClaimsProvider;
import com.mycompany.sample.plumbing.configuration.LoggingConfiguration;
import com.mycompany.sample.plumbing.configuration.OAuthConfiguration;
import com.mycompany.sample.plumbing.logging.LoggerFactory;
import com.mycompany.sample.plumbing.oauth.IssuerMetadata;

/*
 * A class to manage composing core API behaviour
 */
public final class BaseCompositionRoot {

    private final ConfigurableListableBeanFactory container;
    private OAuthConfiguration oauthConfiguration;
    private CustomClaimsProvider customClaimsProvider;
    private LoggingConfiguration loggingConfiguration;
    private LoggerFactory loggerFactory;

    public BaseCompositionRoot(final ConfigurableListableBeanFactory container) {
        this.container = container;
        this.customClaimsProvider = null;
    }

    /*
     * Indicate that we're using OAuth and receive the configuration
     */
    public BaseCompositionRoot useOAuth(final OAuthConfiguration oauthConfiguration) {

        this.oauthConfiguration = oauthConfiguration;
        return this;
    }

    /*
     * Consumers can provide an object for providing custom claims
     */
    public BaseCompositionRoot withCustomClaimsProvider(final CustomClaimsProvider provider) {
        this.customClaimsProvider = provider;
        return this;
    }

    /*
     * Receive the logging configuration so that we can create objects related to logging and error handling
     */
    public BaseCompositionRoot withLogging(
            final LoggingConfiguration loggingConfiguration,
            final LoggerFactory loggerFactory) {

        this.loggingConfiguration = loggingConfiguration;
        this.loggerFactory = loggerFactory;
        return this;
    }

    /*
     * Register and return the authorizer
     */
    public void register() {

        // Register dependencies for logging and error handling
        this.registerBaseDependencies();

        // Register OAuth specific dependencies for Entry Point APIs
        if (this.oauthConfiguration != null) {
            this.registerOAuthDependencies();
        }

        // Register claims dependencies for all APIs
        this.registerClaimsDependencies();
    }

    /*
     * Register dependencies used for logging and error handling, which are natural singletons
     */
    private void registerBaseDependencies() {

        this.container.registerSingleton("LoggingConfiguration", this.loggingConfiguration);
        this.container.registerSingleton("LoggerFactory", this.loggerFactory);
    }

    /*
     * Register dependencies used for OAuth processing
     */
    private void registerOAuthDependencies() {

        // Load metadata if using OAuth security
        var metadata = new IssuerMetadata(this.oauthConfiguration);
        metadata.initialize();

        if (this.oauthConfiguration.getStrategy() == "claims-caching") {

            // this.container.registerResolvableDependency();

        } else {

            // this.container.registerResolvableDependency();
        }

        if (this.oauthConfiguration.getTokenValidationStrategy() == "introspection") {

            // this.container.registerResolvableDependency();

        } else {

            // this.container.registerResolvableDependency();
        }

        // Register these natural singletons
        this.container.registerSingleton("OAuthConfiguration", this.oauthConfiguration);
        this.container.registerSingleton("CustomClaimsProvider", this.customClaimsProvider);
        this.container.registerSingleton("IssuerMetadata", metadata);
    }

    /*
     * Register dependencies used for Claims processing
     */
    private void registerClaimsDependencies() {

        var cache = new ClaimsCache(
                this.oauthConfiguration.getClaimsCacheTimeToLiveMinutes(),
                this.customClaimsProvider,
                this.loggerFactory);
        this.container.registerSingleton("ClaimsCache", cache);
    }
}
