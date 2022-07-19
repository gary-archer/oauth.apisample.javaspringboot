package com.mycompany.sample.host.startup;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import com.mycompany.sample.plumbing.spring.CustomAuthorizationFilter;

/*
 * A class to manage HTTP configuration for our server
 */
@Configuration
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class HttpServerConfiguration {

    private final ConfigurableApplicationContext context;

    public HttpServerConfiguration(final ConfigurableApplicationContext context) {
        this.context = context;
    }

    /*
     * Spring is a web backend technology stack by default, and activates lots of web related behaviour
     * This includes cookie defenses, headers related to web hosting and so on
     * In my architecture these concerns are dealt with in an OAuth Proxy or Web Host, so I disable them here
     * The API needs only to validate JWTs and apply authorization based on claims
     */
    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {

        var container = this.context.getBeanFactory();
        var authorizationFilter = new CustomAuthorizationFilter(container);

        http
                .antMatcher(ResourcePaths.ALL)

                // Customize OAuth processing to use a filter that validates JWTs using the jose4j library
                // This also builds a useful ClaimsPrincipal that is not limited to claims in the JWT access token
                .authorizeRequests(authorize ->
                        authorize.anyRequest().authenticated()
                        .and()
                        .addFilterBefore(
                            authorizationFilter,
                            AbstractPreAuthenticatedProcessingFilter.class))

                // CSRF is dealt with in an OAuth Proxy called from the browser
                .csrf().disable()

                // Security headers are returned from a web host to the browser
                .headers().disable()

                // The API would save session data to a database
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        return http.build();
    }

    /*
     * For the time being the route to look up custom claims allows anonymous access
     * https://github.com/spring-projects/spring-security/issues/10938
     */
    @Bean
    @Order(0)
    public SecurityFilterChain anonymousRoutes(final HttpSecurity http) throws Exception {

        http
                .antMatcher(ResourcePaths.CUSTOMCLAIMS)
                .authorizeRequests(authorize ->
                        authorize.anyRequest().permitAll())
                .csrf().disable()
                .headers().disable()
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        return http.build();
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
