package com.authsamples.api.host.startup;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import com.authsamples.api.logic.claims.ExtraClaims;
import com.authsamples.api.plumbing.spring.CustomAuthorizationFilter;

/*
 * A class to manage OAuth security configuration for the REST API
 */
@Configuration
public class SecurityConfiguration {

    private final ConfigurableApplicationContext context;

    public SecurityConfiguration(final ConfigurableApplicationContext context) {
        this.context = context;
    }

    /*
     * Spring is a web backend technology stack by default, and activates lots of web related behaviour
     * These include cookie defenses, headers related to web hosting and so on
     * In my blog's architecture these concerns are dealt with in an API gateway or Web host, so I disable them here
     * The API needs only to validate JWTs and implement claims based authorization
     */
    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {

        var container = this.context.getBeanFactory();
        var authorizationFilter = new CustomAuthorizationFilter<ExtraClaims>(container);

        http
                // OAuth security for the API is applied via these settings
                .securityMatcher(PathPatternRequestMatcher.withDefaults().matcher(ResourcePaths.ALL))
                .authorizeHttpRequests(authorize ->
                        authorize.anyRequest().authenticated()
                )
                .addFilterBefore(authorizationFilter, AbstractPreAuthenticatedProcessingFilter.class)

                // Disable web host and API gateway concerns, which the API does not implement
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .headers(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .securityContext(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .exceptionHandling(AbstractHttpConfigurer::disable)
                .sessionManagement(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
