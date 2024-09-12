package com.authsamples.api.plumbing.dependencies;

import org.jose4j.jwk.HttpsJwks;
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import com.authsamples.api.plumbing.claims.ClaimsCache;
import com.authsamples.api.plumbing.claims.ExtraClaimsProvider;
import com.authsamples.api.plumbing.configuration.LoggingConfiguration;
import com.authsamples.api.plumbing.configuration.OAuthConfiguration;
import com.authsamples.api.plumbing.logging.LoggerFactory;

/*
 * A class to manage composing core API behaviour
 */
public final class BaseCompositionRoot {

    private final ConfigurableListableBeanFactory container;
    private OAuthConfiguration oauthConfiguration;
    private ExtraClaimsProvider extraClaimsProvider;
    private LoggingConfiguration loggingConfiguration;
    private LoggerFactory loggerFactory;

    public BaseCompositionRoot(final ConfigurableListableBeanFactory container) {
        this.container = container;
        this.extraClaimsProvider = new ExtraClaimsProvider();
    }

    /*
     * Indicate that we're using OAuth and receive the configuration
     */
    public BaseCompositionRoot useOAuth(final OAuthConfiguration oauthConfiguration) {

        this.oauthConfiguration = oauthConfiguration;
        return this;
    }

    /*
     * Optionally provide an object for retrieving extra claims
     */
    public BaseCompositionRoot withExtraClaimsProvider(final ExtraClaimsProvider extraClaimsProvider) {
        this.extraClaimsProvider = extraClaimsProvider;
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

        // Register runtime dependencies for logging and error handling
        this.registerLoggingDependencies();

        // Register runtime dependencies for OAuth and claims handling
        this.registerOAuthDependencies();
        this.registerClaimsDependencies();
    }

    /*
     * Register dependencies used for logging and error handling, which are natural singletons
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

            // Register the configuration
            this.container.registerSingleton("OAuthConfiguration", this.oauthConfiguration);

            // Register a global object that caches JWKS keys
            var httpsJkws = new HttpsJwks(this.oauthConfiguration.getJwksEndpoint());
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
                this.oauthConfiguration.getClaimsCacheTimeToLiveMinutes(),
                this.loggerFactory);
        this.container.registerSingleton("ClaimsCache", cache);
    }
}
