package com.authsamples.api.host;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.authsamples.api.configuration.LoggingConfiguration;
import com.authsamples.api.middleware.errors.CustomHeaderInterceptor;
import com.authsamples.api.middleware.logging.LoggingInterceptor;
import com.authsamples.api.middleware.logging.LoggerFactory;

/*
 * Manages configuration of cross-cutting concerns in interceptors
 */
@Configuration
public class InterceptorConfiguration implements WebMvcConfigurer {

    private final LoggingConfiguration loggingConfiguration;
    private final LoggerFactory loggerFactory;
    private final ConfigurableApplicationContext context;

    public InterceptorConfiguration(
            LoggingConfiguration loggingConfiguration,
            LoggerFactory loggerFactory,
            ConfigurableApplicationContext context) {

        this.loggingConfiguration = loggingConfiguration;
        this.loggerFactory = loggerFactory;
        this.context = context;
    }

    /*
     * Configure cross cutting concerns
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // Add the logging interceptor
        var loggingInterceptor = new LoggingInterceptor(this.context.getBeanFactory(), this.loggerFactory);
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns(ResourcePaths.ALL);

        // Add a custom header interceptor for testing failure scenarios
        var headerInterceptor = new CustomHeaderInterceptor(this.loggingConfiguration.getApiName());
        registry.addInterceptor(headerInterceptor)
                .addPathPatterns(ResourcePaths.ALL);
    }
}
