package com.mycompany.api.basicapi.plumbing.oauth;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;

/*
 * Configure how API requests are authenticated
 */
@Configuration
@EnableResourceServer
public class ApiSecurityConfiguration extends ResourceServerConfigurerAdapter {

    /*
     * Validate OAuth tokens for all API requests except OPTIONS requests
     * Do not try to validate OAuth tokens for web paths under /spa
     */
    @Override
    public void configure(HttpSecurity http) throws Exception {

        http.authorizeRequests()
            .antMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
            .antMatchers("/api/**")
            .authenticated();

        // TODO: I either need to add a filter in the above fluent configuration or add a FilterRegistrationBean
    }

    /*
     * Configure to use a custom token service that manages introspection and claims lookup
     */
    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {

        // TODO: Does Spring support async?
        // https://github.com/spring-projects/spring-security-oauth/issues/736
        //
        // Using generated security password
        // Inject configuration properly
        // HttpClient and proxy
        // Inject the SecurityContextHolder
        //
        // Look at TokenEnhancer to get other stuff
        // https://www.baeldung.com/spring-security-oauth-jwt
        //
        // Connect2id looks like a much better option, via maven
        // https://bitbucket.org/connect2id/oauth-2.0-sdk-with-openid-connect-extensions
        // Introspect is in OAuth library as opposed to OIDC library
        // https://static.javadoc.io/com.nimbusds/oauth2-oidc-sdk/5.0/com/nimbusds/oauth2/sdk/TokenIntrospectionRequest.html
        // https://mvnrepository.com/artifact/com.nimbusds/oauth2-oidc-sdk

        // Get the OAuth token from the authorization bearer header
        resources.tokenExtractor(new BearerTokenExtractor());

        // Return the claims handler
        resources.tokenServices(new ClaimsService());
    }
}
