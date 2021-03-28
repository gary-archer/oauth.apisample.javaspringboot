package com.mycompany.sample.plumbing.dependencies;

import java.net.URI;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import com.mycompany.sample.plumbing.claims.ClaimsCache;
import com.mycompany.sample.plumbing.claims.CustomClaimsProvider;
import com.mycompany.sample.plumbing.configuration.LoggingConfiguration;
import com.mycompany.sample.plumbing.configuration.OAuthConfiguration;
import com.mycompany.sample.plumbing.logging.LoggerFactory;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;

/*
 * A class to manage composing core API behaviour
 */
public final class BaseCompositionRoot {

    private final ConfigurableListableBeanFactory _container;
    private OAuthConfiguration _oauthConfiguration;
    private CustomClaimsProvider _customClaimsProvider;
    private LoggingConfiguration _loggingConfiguration;
    private LoggerFactory _loggerFactory;

    public BaseCompositionRoot(final ConfigurableListableBeanFactory container) {
        this._container = container;
        this._customClaimsProvider = null;
    }

    /*
     * Indicate that we're using OAuth and receive the configuration
     */
    public BaseCompositionRoot useOAuth(final OAuthConfiguration oauthConfiguration) {

        this._oauthConfiguration = oauthConfiguration;
        return this;
    }

    /*
     * Consumers can provide an object for providing custom claims
     */
    public BaseCompositionRoot withCustomClaimsProvider(final CustomClaimsProvider provider) {
        this._customClaimsProvider = provider;
        return this;
    }

    /*
     * Receive the logging configuration so that we can create objects related to logging and error handling
     */
    public BaseCompositionRoot withLogging(
            final LoggingConfiguration loggingConfiguration,
            final LoggerFactory loggerFactory) {

        this._loggingConfiguration = loggingConfiguration;
        this._loggerFactory = loggerFactory;
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

        this._container.registerSingleton("LoggingConfiguration", this._loggingConfiguration);
        this._container.registerSingleton("LoggerFactory", this._loggerFactory);
    }

    /*
     * Register dependencies used for OAuth processing
     */
    private void registerOAuthDependencies() {

        try {

            this._container.registerSingleton("OAuthConfiguration", this._oauthConfiguration);
            this._container.registerSingleton("CustomClaimsProvider", this._customClaimsProvider);

            // Inject the claims cache if using this strategy
            if (this._oauthConfiguration.get_strategy().equals("claims-caching")) {

                var cache = new ClaimsCache(
                        this._oauthConfiguration.get_claimsCacheTimeToLiveMinutes(),
                        this._customClaimsProvider,
                        this._loggerFactory);
                this._container.registerSingleton("ClaimsCache", cache);
            }

            // Use a global object that caches JWKS keys if using this strategy
            if (this._oauthConfiguration.get_tokenValidationStrategy().equals("jwt")) {

                var jwksUri = new URI(this._oauthConfiguration.get_jwksEndpoint());
                var jwksKeySet = new RemoteJWKSet<>(jwksUri.toURL());
                this._container.registerSingleton("JWKSKeySet", jwksKeySet);
            }

        } catch (Throwable ex) {

            throw new RuntimeException("Problem encountered registering OAuth dependencies", ex);
        }
    }
}
