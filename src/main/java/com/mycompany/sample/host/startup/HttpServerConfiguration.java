package com.mycompany.sample.host.startup;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import com.mycompany.sample.host.configuration.ApiConfiguration;
import com.mycompany.sample.plumbing.spring.CustomAuthorizationFilter;

/*
 * A class to manage HTTP configuration for our server
 */
@Configuration
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class HttpServerConfiguration extends WebSecurityConfigurerAdapter {

    private final ApiConfiguration apiConfiguration;
    private final ConfigurableApplicationContext context;

    public HttpServerConfiguration(
            final ApiConfiguration apiConfiguration,
            final ConfigurableApplicationContext context) {

        this.apiConfiguration = apiConfiguration;
        this.context = context;
    }

    /*
     * Configure API security via a custom authorization filter which allows us to take full control
     */
    @Override
    public void configure(final HttpSecurity http) throws Exception {

        var container = this.context.getBeanFactory();
        var authorizationFilter = new CustomAuthorizationFilter(container);

        http
                .antMatcher(ResourcePaths.ALL)
                .authorizeRequests()
                    .antMatchers(ResourcePaths.CUSTOMCLAIMS).permitAll()
                    .anyRequest().authenticated()
                    .and()
                .addFilterBefore(
                    authorizationFilter,
                    AbstractPreAuthenticatedProcessingFilter.class)
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    /*
     * This is necessary to prevent requests to anonymous endpoints from requiring an access token
     */
    @Override
    public void configure(final WebSecurity webSecurity) {
        webSecurity.ignoring().antMatchers(ResourcePaths.CUSTOMCLAIMS);
    }

    /*
     * My current Authorization Server calls the claims controller without URL encoding so allow this temporarily
     */
    @Bean
    public HttpFirewall allowUrlEncodedSlashHttpFirewall() {

        var firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedPercent(true);
        return firewall;
    }
}
