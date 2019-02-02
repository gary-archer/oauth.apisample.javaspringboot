package com.mycompany.api.basicapi.startup;

import com.mycompany.api.basicapi.entities.BasicApiClaims;
import com.mycompany.api.basicapi.plumbing.oauth.AuthorizationFilter;
import com.mycompany.api.basicapi.plumbing.oauth.ClaimsCache;
import com.mycompany.api.basicapi.plumbing.oauth.IssuerMetadata;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.resource.PathResourceResolver;

/*
 * A class to manage HTTP configuration for our server
 */
@Configuration
public class HttpServerConfiguration extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {

    /*
     * Injected dependencies
     */
    private final com.mycompany.api.basicapi.configuration.Configuration configuration;
    private final IssuerMetadata metadata;
    private final ClaimsCache cache;

    /*
     * Receive dependencies
     */
    public HttpServerConfiguration(
            com.mycompany.api.basicapi.configuration.Configuration configuration,
            IssuerMetadata metadata,
            ClaimsCache cache)
    {
        this.configuration = configuration;
        this.metadata = metadata;
        this.cache = cache;
    }

    /*
     * Configure how API requests are secured
     * I am using a OncePerRequestFilter as opposed to providing a custom ResourceServerTokenServices
     * This is due to problems where the latter fires again when a CompletableFuture moves to the ASYNC / completed stage
     */
    @Override
    public void configure(HttpSecurity http) throws Exception {

        // Create a Spring security filter and give it our singleton objects
        // We use a supplier method for creating our custom claims in common code
        var authorizationFilter = new AuthorizationFilter(this.configuration, this.metadata, this.cache, () -> new BasicApiClaims());

        // Indicate that API requests use the filter
        http.antMatcher("/api/**")
                .authorizeRequests()
                .anyRequest()
                .authenticated()
                .and()
                .addFilterBefore(authorizationFilter, AbstractPreAuthenticatedProcessingFilter.class);
    }

    /*
     * Ensure that OPTIONS requests are not passed to the above filter
     */
    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers(HttpMethod.OPTIONS, "/api/**");
    }

    /*
     * Configure our API to allow cross origin requests from our SPA
     */
    @Override
    public void addCorsMappings(CorsRegistry registry)
    {
        var registration = registry.addMapping("/api/**");

        var trustedOrigins = this.configuration.getApp().getTrustedOrigins();
        for (var trustedOrigin : trustedOrigins) {
            registration.allowedOrigins(trustedOrigin);
        }
    }

    /*
     * A primitive web server to serve our SPA's static content
     * https://visola.github.io/2018/01/17/spa-with-spring-boot/index.html
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // Point to the root 'spa' folder and its static content
        registry.addResourceHandler("/**/*.js", "/**/*.css", "/**/*.svg", "/**/*.ico", "/**/*.json")
                .setCachePeriod(0)
                .addResourceLocations("file:../spa");

        // Point to 'spa' folder and its index.html file
        registry.addResourceHandler("spa", "spa/")
                .setCachePeriod(0)
                .addResourceLocations("file:../spa/index.html")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) {
                        return location.exists() && location.isReadable() ? location : null;
                    }
                });
    }
}
