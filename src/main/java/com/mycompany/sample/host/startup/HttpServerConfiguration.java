package com.mycompany.sample.host.startup;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.mycompany.sample.host.configuration.ApiConfiguration;
import com.mycompany.sample.plumbing.configuration.LoggingConfiguration;
import com.mycompany.sample.plumbing.interceptors.CustomHeaderInterceptor;
import com.mycompany.sample.plumbing.interceptors.LoggingInterceptor;
import com.mycompany.sample.plumbing.security.CustomAuthenticationEntryPoint;
import com.mycompany.sample.plumbing.security.CustomAuthenticationManager;
import com.mycompany.sample.plumbing.security.CustomBearerTokenResolver;

/*
 * A class to manage HTTP configuration for our server
 */
@org.springframework.context.annotation.Configuration
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class HttpServerConfiguration extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {

    private final ApiConfiguration apiConfiguration;
    private final LoggingConfiguration loggingConfiguration;
    private final ConfigurableApplicationContext context;
    private final String apiRequestPaths = "/api/**";

    public HttpServerConfiguration(
            final ApiConfiguration apiConfiguration,
            final LoggingConfiguration loggingConfiguration,
            final ConfigurableApplicationContext context) {

        this.apiConfiguration = apiConfiguration;
        this.loggingConfiguration = loggingConfiguration;
        this.context = context;
    }

    /*
     * Configure API security according to 2020 Spring Security patterns
     * https://github.com/spring-projects/spring-security/wiki/OAuth-2.0-Migration-Guide
     */
    @Override
    public void configure(final HttpSecurity http) throws Exception {

        var container = this.context.getBeanFactory();
        http
                .antMatcher(this.apiRequestPaths)
                .cors()
                    .and()
                .authorizeRequests()
                    .anyRequest()
                    .authenticated()
                    .and()
                .oauth2ResourceServer()
                    .bearerTokenResolver(new CustomBearerTokenResolver())
                    .authenticationManagerResolver(request -> new CustomAuthenticationManager(container, request))
                    .authenticationEntryPoint(new CustomAuthenticationEntryPoint(container))
                    .and()
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    /*
     * Configure our API to allow cross origin requests from our SPA
     */
    @Override
    public void addCorsMappings(final CorsRegistry registry) {

        var registration = registry.addMapping(this.apiRequestPaths);
        var trustedOrigins = this.apiConfiguration.getWebTrustedOrigins();
        for (var trustedOrigin: trustedOrigins) {
            registration.allowedOrigins(trustedOrigin);
        }
    }

    /*
     * Configure cross cutting concerns
     */
    @Override
    public void addInterceptors(final InterceptorRegistry registry) {

        // Add the logging interceptor
        var loggingInterceptor = new LoggingInterceptor(this.context.getBeanFactory());
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns(this.apiRequestPaths);

        // Add a custom header interceptor for testing failure scenarios
        var headerInterceptor = new CustomHeaderInterceptor(this.loggingConfiguration.getApiName());
        registry.addInterceptor(headerInterceptor)
                .addPathPatterns(this.apiRequestPaths);
    }
}
