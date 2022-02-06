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

/*
 * A helper class to manage creating strategy based OAuth objects at runtime
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class OAuthInjector {

    private final ConfigurableListableBeanFactory container;
    private final OAuthConfiguration configuration;

    public OAuthInjector(
            final ConfigurableApplicationContext context,
            final OAuthConfiguration configuration) {

        this.container = context.getBeanFactory();
        this.configuration = configuration;
    }

    /*
     * Create the configured type of authorizer at runtime
     */
    @Bean
    @Scope(value = CustomRequestScope.NAME)
    public Authorizer createAuthorizer() {

        var authenticator = this.container.getBean(OAuthAuthenticator.class);
        var customClaimsProvider = this.container.getBean(CustomClaimsProvider.class);

        if (this.configuration.getProvider().equals("cognito")) {

            var cache = this.container.getBean(ClaimsCache.class);
            return new ClaimsCachingAuthorizer(cache, authenticator, customClaimsProvider);

        } else {

            return new StandardAuthorizer(authenticator, customClaimsProvider);
        }
    }
}
