package com.mycompany.sample.plumbing.dependencies;

import java.net.URI;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import com.mycompany.sample.plumbing.claims.ClaimsCache;
import com.mycompany.sample.plumbing.claims.ClaimsProvider;
import com.mycompany.sample.plumbing.configuration.LoggingConfiguration;
import com.mycompany.sample.plumbing.configuration.OAuthConfiguration;
import com.mycompany.sample.plumbing.logging.LoggerFactory;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;

/*
 * A class to manage composing core API behaviour
 */
public final class BaseCompositionRoot {

    private final ConfigurableListableBeanFactory container;
    private OAuthConfiguration oauthConfiguration;
    private ClaimsProvider claimsProvider;
    private LoggingConfiguration loggingConfiguration;
    private LoggerFactory loggerFactory;

    public BaseCompositionRoot(final ConfigurableListableBeanFactory container) {
        this.container = container;
        this.claimsProvider = null;
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
    public BaseCompositionRoot withClaimsProvider(final ClaimsProvider provider) {
        this.claimsProvider = provider;
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

            this.container.registerSingleton("OAuthConfiguration", this.oauthConfiguration);
            this.container.registerSingleton("CustomClaimsProvider", this.claimsProvider);

            // Inject the claims cache if using this strategy
            if (this.oauthConfiguration.getStrategy().equals("claims-caching")) {

                var cache = new ClaimsCache(
                        this.oauthConfiguration.getClaimsCacheTimeToLiveMinutes(),
                        this.claimsProvider,
                        this.loggerFactory);
                this.container.registerSingleton("ClaimsCache", cache);
            }

            // Use a global object that caches JWKS keys if using this strategy
            if (this.oauthConfiguration.getTokenValidationStrategy().equals("jwt")) {

                var jwksUri = new URI(this.oauthConfiguration.getJwksEndpoint());
                var jwksKeySet = new RemoteJWKSet<>(jwksUri.toURL());
                this.container.registerSingleton("JWKSKeySet", jwksKeySet);
            }

        } catch (Throwable ex) {

            throw new RuntimeException("Problem encountered registering OAuth dependencies", ex);
        }
    }
}
