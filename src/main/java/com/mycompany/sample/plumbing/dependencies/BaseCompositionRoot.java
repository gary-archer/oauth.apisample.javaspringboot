package com.mycompany.sample.plumbing.dependencies;

import org.jose4j.jwk.HttpsJwks;
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import com.mycompany.sample.plumbing.claims.ClaimsCache;
import com.mycompany.sample.plumbing.claims.CustomClaimsProvider;
import com.mycompany.sample.plumbing.configuration.LoggingConfiguration;
import com.mycompany.sample.plumbing.configuration.OAuthConfiguration;
import com.mycompany.sample.plumbing.logging.LoggerFactory;

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
    public BaseCompositionRoot withClaimsProvider(final CustomClaimsProvider customClaimsProvider) {
        this.customClaimsProvider = customClaimsProvider;
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
            this.container.registerSingleton("CustomClaimsProvider", this.customClaimsProvider);

            // Inject the claims cache if using this strategy
            if (this.oauthConfiguration.getProvider().equals("cognito")) {

                var cache = new ClaimsCache(
                        this.oauthConfiguration.getClaimsCacheTimeToLiveMinutes(),
                        this.customClaimsProvider,
                        this.loggerFactory);
                this.container.registerSingleton("ClaimsCache", cache);
            }

            // Register a global object that caches JWKS keys
            var httpsJkws = new HttpsJwks(this.oauthConfiguration.getJwksEndpoint());
            var jwksKeyResolver = new HttpsJwksVerificationKeyResolver(httpsJkws);
            this.container.registerSingleton("JwksResolver", jwksKeyResolver);

        } catch (Throwable ex) {

            throw new RuntimeException("Problem encountered registering OAuth dependencies", ex);
        }
    }
}
