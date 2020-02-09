package com.mycompany.sample.framework.api.base.startup;

import com.mycompany.sample.framework.api.base.security.HeaderAuthorizer;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.web.filter.OncePerRequestFilter;

/*
 * Build a simple authorizer for receiving claims via headers
 */
public final class HeaderAuthorizerBuilder {

    private final ConfigurableListableBeanFactory container;

    public HeaderAuthorizerBuilder(final ConfigurableListableBeanFactory container) {
        this.container = container;
    }

    /*
     * Register and return the authorizer
     */
    public OncePerRequestFilter register() {
        var authorizer = new HeaderAuthorizer(this.container);
        this.registerSingletonDependencies(authorizer);
        return authorizer;
    }

    /*
     * Register security dependencies at application startup
     */
    private void registerSingletonDependencies(final HeaderAuthorizer authorizer) {
        this.container.registerSingleton("Authorizer", authorizer);
    }
}
