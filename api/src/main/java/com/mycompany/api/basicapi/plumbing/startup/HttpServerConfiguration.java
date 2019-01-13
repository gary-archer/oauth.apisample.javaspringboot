package com.mycompany.api.basicapi.plumbing.startup;

import com.ea.async.Async;
import com.mycompany.api.basicapi.plumbing.threading.AsyncRequestTaskDecorator;
import com.mycompany.api.basicapi.plumbing.threading.AsyncRequestThreadPoolTaskExecutor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.resource.PathResourceResolver;

/*
 * A class to manage HTTP configuration
 *
 */
@Configuration
public class HttpServerConfiguration extends WebMvcConfigurationSupport {

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
     * Custom configuration to allow request scope objects to be accessed during async completion
     * https://jira.spring.io/browse/SPR-6873
     * https://stackoverflow.com/questions/23732089/how-to-enable-request-scope-in-async-task-executor/33337838#33337838
     */
    @Override
    protected void configureAsyncSupport(AsyncSupportConfigurer configurer) {

        /// This gets called but it seems to be too late and may need to happen before the web server starts
        System.out.println("**** ASYNC STARTUP");

        // Now initialise the EA async await library
        System.out.println("*** MAIN: Starting EA async");
        Async.init();

        AsyncRequestThreadPoolTaskExecutor executor = new AsyncRequestThreadPoolTaskExecutor();
        executor.setTaskDecorator(new AsyncRequestTaskDecorator());
        executor.initialize();

        // Return the executor
        configurer.setTaskExecutor(executor);
        // configurer.registerCallableInterceptors().setTaskExecutor(executor);
    }

    /*
     * Configure our API to allow cross origin requests from our SPA
     */
    @Override
    public void addCorsMappings(CorsRegistry registry)
    {
        System.out.println("**** CORS STARTUP");
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

        System.out.println("**** WEB SERVER STARTUP");

        // Point to the root 'spa' folder and its static content
        registry.addResourceHandler("/**/*.js", "/**/*.css", "/**/*.svg", "/**/*.json")
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
