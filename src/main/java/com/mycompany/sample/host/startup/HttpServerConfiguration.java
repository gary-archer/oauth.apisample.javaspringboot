package com.mycompany.sample.host.startup;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import com.mycompany.sample.plumbing.spring.CustomAuthorizationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/*
 * A class to manage HTTP configuration for our server
 */
@Configuration
@EnableWebSecurity
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class HttpServerConfiguration {

    private final ConfigurableApplicationContext context;

    public HttpServerConfiguration(final ConfigurableApplicationContext context) {
        this.context = context;
    }

    /*
     * Spring is a web backend technology stack by default, and activates lots of web related behaviour
     * EnableWebSecurity by default results in cookie defenses, headers related to web hosting and so on
     * In my architecture these concerns are dealt with in an OAuth Proxy or Web Host, so I disable them here
     * The API needs only to validate JWTs and apply authorization based on claims
     */
    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {

        var container = this.context.getBeanFactory();
        var authorizationFilter = new CustomAuthorizationFilter(container);

        http
                // Configure OAuth API security for these endpoints
                .securityMatcher(new AntPathRequestMatcher(ResourcePaths.ALL))
                .authorizeHttpRequests(authorize ->
                        authorize.anyRequest().authenticated()
                        .and()
                        .addFilterBefore(
                                authorizationFilter,
                                AbstractPreAuthenticatedProcessingFilter.class))

                // Disable web host and API gateway concerns
                .csrf().disable()
                .headers().disable()
                .requestCache().disable()
                .securityContext().disable()
                .logout().disable()
                .exceptionHandling().disable()
                .sessionManagement().disable();

        return http.build();
    }

    /*
     * For the time being the route to look up custom claims uses anonymous access
     * https://github.com/spring-projects/spring-security/issues/10938
     */
    @Bean
    @Order(0)
    public SecurityFilterChain anonymousRoutes(final HttpSecurity http) throws Exception {

        http
                // Configure anonymous security for this endpoint
                .securityMatcher(new AntPathRequestMatcher(ResourcePaths.CUSTOMCLAIMS))
                .authorizeHttpRequests(authorize ->
                        authorize.anyRequest().permitAll())

                // Disable web host and API gateway concerns
                .csrf().disable()
                .headers().disable()
                .requestCache().disable()
                .securityContext().disable()
                .logout().disable()
                .exceptionHandling().disable()
                .sessionManagement().disable();

        return http.build();
    }
}
