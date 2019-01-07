package com.mycompany.api.basicapi.plumbing.oauth;

import org.mitre.oauth2.introspectingfilter.IntrospectingTokenService;
import org.mitre.oauth2.introspectingfilter.service.impl.StaticIntrospectionConfigurationService;
import org.mitre.oauth2.model.RegisteredClient;
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
    }

    /*
     * Configure to use a custom token service that manages introspection and claims
     * https://stackoverflow.com/questions/42725605/how-to-protect-a-resource-using-spring-security-oauth2-and-mitreid-connect-intro
     */
    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {

        // Async article
        // https://github.com/spring-projects/spring-security-oauth/issues/736


        // Get the OAuth token from the authorization bearer header
        resources.tokenExtractor(new BearerTokenExtractor());

        // Supply the URL
        StaticIntrospectionConfigurationService introspectConfig = new StaticIntrospectionConfigurationService();
        introspectConfig.setIntrospectionUrl("https://dev-843469.oktapreview.com/oauth2/default/v1/introspect");

        // Supply the credentials needed for introspection
        RegisteredClient client = new RegisteredClient();
        client.setClientId("0oac5s69rjXE0HcZO0h7");
        client.setClientSecret("VEEe9m9WDneeUSUJUOMKZiso_X6xwKvAqRInT2kg");
        // client.setTokenEndpointAuthMethod(AuthMethod.NONE);

        // Return the Spring Boot service
        IntrospectingTokenService introspectTokenService = new IntrospectingTokenService();
        introspectTokenService.setIntrospectionConfigurationService(introspectConfig);
        resources.tokenServices(introspectTokenService);
    }
}
