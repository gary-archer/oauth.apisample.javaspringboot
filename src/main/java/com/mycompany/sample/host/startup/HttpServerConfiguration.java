package com.mycompany.sample.host.startup;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import jakarta.servlet.DispatcherType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import com.mycompany.sample.plumbing.spring.CustomAuthorizationFilter;

/*
 * A class to manage HTTP configuration for our server
 */
@Configuration
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class HttpServerConfiguration implements AsyncConfigurer {

    private final ConfigurableApplicationContext context;

    public HttpServerConfiguration(final ConfigurableApplicationContext context) {
        this.context = context;
    }

    /*
     * Spring is a web backend technology stack by default, and activates lots of web related behaviour
     * EnableWebSecurity by default results in cookie defenses, headers related to web hosting and so on
     * In my blog's architecture these concerns are dealt with in an API gateway or Web host, so I disable them here
     * The API needs only to validate JWTs and apply authorization based on claims
     */
    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {

        var container = this.context.getBeanFactory();
        var authorizationFilter = new CustomAuthorizationFilter(container);

        http
                .securityMatcher(new AntPathRequestMatcher(ResourcePaths.ALL))
                .authorizeHttpRequests(authorize ->

                        // This seems to be required in Spring Boot 3, to work around cryptic async errors
                        // https://github.com/spring-projects/spring-security/issues/11962
                        authorize.dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()

                        // The OAuth security for the API is applied via these settings
                        .anyRequest().authenticated()
                )
                .addFilterBefore(authorizationFilter, AbstractPreAuthenticatedProcessingFilter.class)

                // Disable web host and API gateway concerns
                .csrf(AbstractHttpConfigurer::disable)
                .headers(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .securityContext(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .exceptionHandling(AbstractHttpConfigurer::disable)
                .sessionManagement(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Override
    public Executor getAsyncExecutor() {
        return new DelegatingSecurityContextExecutorService(Executors.newFixedThreadPool(5));
    }
}
