package com.authsamples.api.host.dependencies;

import org.jose4j.jwk.HttpsJwks;
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import com.authsamples.api.host.configuration.Configuration;
import com.authsamples.api.plumbing.claims.ClaimsCache;
import com.authsamples.api.plumbing.claims.ExtraClaimsProvider;
import com.authsamples.api.plumbing.configuration.LoggingConfiguration;
import com.authsamples.api.plumbing.logging.LoggerFactory;

/*
 * Dependency injection composition
 */
public final class CompositionRoot {

    private final ConfigurableListableBeanFactory container;
    private Configuration configuration;
    private ExtraClaimsProvider extraClaimsProvider;
    private LoggingConfiguration loggingConfiguration;
    private LoggerFactory loggerFactory;

    /*
     * Receive the DI container
     */
    public CompositionRoot(final ConfigurableListableBeanFactory container) {
        this.container = container;
    }

    /*
     * Receive configuration
     */
    public CompositionRoot addConfiguration(final Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /*
     * Receive an object that customizes the claims principal
     */
    public CompositionRoot addExtraClaimsProvider(final ExtraClaimsProvider extraClaimsProvider) {
        this.extraClaimsProvider = extraClaimsProvider;
        return this;
    }

    /*
     * Receive the logging configuration
     */
    public CompositionRoot addLogging(
            final LoggingConfiguration loggingConfiguration,
            final LoggerFactory loggerFactory) {

        this.loggingConfiguration = loggingConfiguration;
        this.loggerFactory = loggerFactory;
        return this;
    }

    /*
     * Register objects that cannot be managed simply by a Spring annotation
     */
    public void register() {

        // Register runtime dependencies for logging and error handling
        this.registerLoggingDependencies();

        // Register runtime dependencies for OAuth and claims handling
        this.registerOAuthDependencies();
        this.registerClaimsDependencies();

        // Register objects needed by application logic
        this.registerApplicationDependencies();
    }

    /*
     * Register dependencies used for logging and error handling
     */
    private void registerLoggingDependencies() {

        this.container.registerSingleton("LoggingConfiguration", this.loggingConfiguration);
        this.container.registerSingleton("LoggerFactory", this.loggerFactory);
    }

    /*
     * Register dependencies used for OAuth processing
     */
    private void registerOAuthDependencies() {

        try {

            // Register the OAuth configuration
            this.container.registerSingleton("OAuthConfiguration", this.configuration.getOauth());

            // Register a global object that caches JWKS keys
            var httpsJkws = new HttpsJwks(this.configuration.getOauth().getJwksEndpoint());
            var jwksKeyResolver = new HttpsJwksVerificationKeyResolver(httpsJkws);
            this.container.registerSingleton("JwksResolver", jwksKeyResolver);

        } catch (Throwable ex) {

            throw new RuntimeException("Problem encountered registering OAuth dependencies", ex);
        }
    }

    /*
     * Register claims related dependencies
     */
    private void registerClaimsDependencies() {

        // Register an object to provide extra claims
        this.container.registerSingleton("ExtraClaimsProvider", this.extraClaimsProvider);

        // Register a cache for extra claims from the API's own data
        var cache = new ClaimsCache(
                this.extraClaimsProvider,
                this.configuration.getOauth().getClaimsCacheTimeToLiveMinutes(),
                this.loggerFactory);
        this.container.registerSingleton("ClaimsCache", cache);
    }

    /*
     * Register objects used by application logic
     */
    private void registerApplicationDependencies() {
        container.registerSingleton("ApiConfiguration", this.configuration.getApi());
    }
}
