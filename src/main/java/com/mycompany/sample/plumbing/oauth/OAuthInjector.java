package com.mycompany.sample.plumbing.oauth;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.mycompany.sample.plumbing.claims.ClaimsCache;
import com.mycompany.sample.plumbing.claims.CustomClaimsProvider;
import com.mycompany.sample.plumbing.configuration.OAuthConfiguration;
import com.mycompany.sample.plumbing.dependencies.CustomRequestScope;
import com.mycompany.sample.plumbing.logging.LogEntryImpl;
import com.mycompany.sample.plumbing.oauth.tokenvalidation.IntrospectionValidator;
import com.mycompany.sample.plumbing.oauth.tokenvalidation.JwtValidator;
import com.mycompany.sample.plumbing.oauth.tokenvalidation.TokenValidator;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;

/*
 * A helper class to manage creating OAuth related objects at runtime
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class OAuthInjector {

    private final ConfigurableListableBeanFactory container;
    private final OAuthConfiguration configuration;

    public OAuthInjector(final ConfigurableApplicationContext context) {

        this.container = context.getBeanFactory();
        this.configuration = this.container.getBean(OAuthConfiguration.class);
    }

    /*
     * Create the configured type of authorizer at runtime
     */
    @Bean
    @Scope(value = CustomRequestScope.NAME)
    public Authorizer createAuthorizer() {

        if (this.configuration.get_strategy().equals("claims-caching")) {

            var cache = this.container.getBean(ClaimsCache.class);
            var authenticator = this.container.getBean(OAuthAuthenticator.class);
            var customClaimsProvider = this.container.getBean(CustomClaimsProvider.class);
            var logEntry = this.container.getBean(LogEntryImpl.class);
            return new ClaimsCachingAuthorizer(cache, authenticator, customClaimsProvider, logEntry);

        } else {

            var authenticator = this.container.getBean(OAuthAuthenticator.class);
            var customClaimsProvider = this.container.getBean(CustomClaimsProvider.class);
            return new StandardAuthorizer(authenticator, customClaimsProvider);
        }
    }

    /*
     * Create the configured type of token validator at runtime
     */
    @Bean
    @Scope(value = CustomRequestScope.NAME)
    public TokenValidator createTokenValidator() {

        if (this.configuration.get_tokenValidationStrategy().equals("introspection")) {

            return new IntrospectionValidator(this.configuration);

        } else {

            var jwksKeySet = this.container.getBean(RemoteJWKSet.class);
            return new JwtValidator(this.configuration, jwksKeySet);
        }
    }
}
