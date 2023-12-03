package com.mycompany.sample.host.startup;

import jakarta.servlet.DispatcherType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import com.mycompany.sample.plumbing.spring.CustomAuthorizationFilter;

/*
 * A class to manage OAuth security configuration for the REST API
 */
@Configuration
@SuppressWarnings(value = "checkstyle:DesignForExtension")
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
        var authorizationFilter = new CustomAuthorizationFilter(container);

        http
                .securityMatcher(new AntPathRequestMatcher(ResourcePaths.ALL))
                .authorizeHttpRequests(authorize ->

                        // This seems to be required in Spring Boot 3, is using async calls during a request
                        // https://github.com/spring-projects/spring-security/issues/11962
                        authorize.dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()

                        // The OAuth security for the API is applied via these settings
                        .anyRequest().authenticated()
                )
                .addFilterBefore(authorizationFilter, AbstractPreAuthenticatedProcessingFilter.class)

                // Disable web host and API gateway concerns
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
