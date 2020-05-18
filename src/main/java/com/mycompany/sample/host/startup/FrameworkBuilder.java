package com.mycompany.sample.host.startup;

import com.mycompany.sample.host.configuration.FrameworkConfiguration;
import com.mycompany.sample.host.plumbing.errors.ApplicationExceptionHandler;
import com.mycompany.sample.host.plumbing.interceptors.CustomHeaderInterceptor;
import com.mycompany.sample.host.plumbing.utilities.RequestClassifier;
import com.mycompany.sample.host.plumbing.logging.LoggerFactory;
import com.mycompany.sample.host.plumbing.interceptors.LoggingInterceptor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

/*
 * A builder style class to configure framework behaviour and to register its dependencies
 */
public final class FrameworkBuilder {

    // Injected properties
    private final ConfigurableListableBeanFactory container;
    private final FrameworkConfiguration configuration;
    private final LoggerFactory loggerFactory;

    // Properties set during building
    private String apiBasePath;
    private ApplicationExceptionHandler applicationExceptionHandler;
    private RequestClassifier requestClassifier;


    /*
     * Receive configuration and initialize properties
     */
    public FrameworkBuilder(
            final ConfigurableListableBeanFactory container,
            final FrameworkConfiguration configuration,
            final LoggerFactory loggerFactory) {

        this.container = container;
        this.configuration = configuration;
        this.loggerFactory = loggerFactory;

        // Set defaults
        this.apiBasePath = null;
        this.applicationExceptionHandler = new ApplicationExceptionHandler();
    }

    /*
     * Record the API base path
     */
    public FrameworkBuilder withApiBasePath(final String apiBasePath) {

        this.apiBasePath = apiBasePath.toLowerCase();
        if (!this.apiBasePath.endsWith("/")) {
            this.apiBasePath += '/';
        }

        return this;
    }

    /*
     * Allow an application handler to translate errors before the framework handler runs
     */
    public FrameworkBuilder withApplicationExceptionHandler(final ApplicationExceptionHandler handler) {
        this.applicationExceptionHandler = handler;
        return this;
    }

    /*
     * Add framework cross cutting concerns
     */
    public FrameworkBuilder addInterceptors(final InterceptorRegistry registry) {

        // Add the framework logging interceptor
        var loggingInterceptor = new LoggingInterceptor(this.container);
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/api/**");

        // Add the framework interceptor for testing failure scenarios
        var headerInterceptor = new CustomHeaderInterceptor(this.container, this.configuration.getApiName());
        registry.addInterceptor(headerInterceptor);

        return this;
    }

    /*
     * Do the main builder work of registering dependencies
     */
    public FrameworkBuilder register() {

        // Create an object used to prevent interceptors from processing SPA and OPTIONS requests
        this.requestClassifier = new RequestClassifier(this.apiBasePath);

        // Register framework specific dependencies
        this.registerSingletonDependencies();
        return this;
    }

    /*
     * Register framework dependencies at application startup
     */
    private void registerSingletonDependencies() {

        // Register framework configuration
        this.container.registerSingleton("FrameworkConfiguration", this.configuration);

        // Register logging objects
        this.container.registerSingleton("LoggerFactory", this.loggerFactory);

        // Register a utility to classify requests based on the api base path
        this.container.registerSingleton("RequestClassifier", this.requestClassifier);

        // Register a utility to allow the API to do its own error translation
        this.container.registerSingleton("ApplicationExceptionHandler", this.applicationExceptionHandler);
    }
}
