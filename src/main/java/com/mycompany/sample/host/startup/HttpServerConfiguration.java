package com.mycompany.sample.host.startup;

import java.util.Arrays;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.mycompany.sample.host.configuration.ApiConfiguration;
import com.mycompany.sample.plumbing.configuration.LoggingConfiguration;
import com.mycompany.sample.plumbing.interceptors.CustomHeaderInterceptor;
import com.mycompany.sample.plumbing.interceptors.LoggingInterceptor;
import com.mycompany.sample.plumbing.spring.CustomAuthorizationFilter;

/*
 * A class to manage HTTP configuration for our server
 */
@Configuration
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
     * Configure API security via a custom authorization filter which allows us to take full control
     */
    @Override
    public void configure(final HttpSecurity http) throws Exception {

        var container = this.context.getBeanFactory();
        var authorizationFilter = new CustomAuthorizationFilter(container);

        http
                .antMatcher(this.apiRequestPaths)
                .cors()
                    .configurationSource(this.getCorsConfiguration())
                    .and()
                .authorizeRequests()
                    .antMatchers("/api/customclaims/**").permitAll()
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
        webSecurity.ignoring().antMatchers("/api/customclaims/**");
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

    /*
     * My current Authorization Server calls the claims controller without URL encoding so allow this temporarily
     */
    @Bean
    public HttpFirewall allowUrlEncodedSlashHttpFirewall() {

        var firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedPercent(true);
        return firewall;
    }

    /*
     * Configure our API to allow cross origin requests from our SPA
     */
    private CorsConfigurationSource getCorsConfiguration() {

        var configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(this.apiConfiguration.getWebTrustedOrigins()));
        configuration.applyPermitDefaultValues();

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(this.apiRequestPaths, configuration);
        return source;
    }
}
