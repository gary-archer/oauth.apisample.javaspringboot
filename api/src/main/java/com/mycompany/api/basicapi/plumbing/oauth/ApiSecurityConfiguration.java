package com.mycompany.api.basicapi.plumbing.oauth;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;

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
     * Configure to use a custom token service that manages introspection and claims lookup
     */
    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {

        resources.tokenServices(new ClaimsService());
    }
}
