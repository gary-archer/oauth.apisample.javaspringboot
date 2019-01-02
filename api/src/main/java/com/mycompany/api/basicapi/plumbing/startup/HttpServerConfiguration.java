package com.mycompany.api.basicapi.plumbing.startup;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/*
 * A class to manage HTTP configuration
 *
 */
@Configuration
public class HttpServerConfiguration implements WebMvcConfigurer {

    /*
     * The injected configuration
     */
    private final com.mycompany.api.basicapi.configuration.Configuration configuration;

    /*
     * Receive our JSON configuration
     */
    public HttpServerConfiguration(com.mycompany.api.basicapi.configuration.Configuration configuration)
    {
        this.configuration = configuration;
    }

    /*
     * Configure our API to allow CORS requests from our SPA
     */
    @Override
    public void addCorsMappings(CorsRegistry registry)
    {
        CorsRegistration registration = registry.addMapping("/api/**");

        String[] trustedOrigins = this.configuration.app.trustedOrigins;
        for (String trustedOrigin : trustedOrigins) {
            registration.allowedOrigins(trustedOrigin);
        }
    }

    /*
     * A primitive web server to serve our SPA's static content
     * https://visola.github.io/2018/01/17/spa-with-spring-boot/index.html
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/**/*.js", "/**/*.css", "/**/*.svg", "/**/*.json")
                .setCachePeriod(0)
                .addResourceLocations("file:../spa");

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
