package com.mycompany.sample.host.startup;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.mycompany.sample.host.configuration.ApiConfiguration;
import com.mycompany.sample.plumbing.configuration.LoggingConfiguration;
import com.mycompany.sample.plumbing.interceptors.CustomHeaderInterceptor;
import com.mycompany.sample.plumbing.interceptors.LoggingInterceptor;

/*
 * Manages configuration of cross-cutting concerns in interceptors
 */
@Configuration
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class InterceptorConfiguration implements WebMvcConfigurer {

    private final LoggingConfiguration loggingConfiguration;
    private final ConfigurableApplicationContext context;

    public InterceptorConfiguration(
            final ApiConfiguration apiConfiguration,
            final LoggingConfiguration loggingConfiguration,
            final ConfigurableApplicationContext context) {

        this.loggingConfiguration = loggingConfiguration;
        this.context = context;
    }

    /*
     * Configure cross cutting concerns
     */
    @Override
    public void addInterceptors(final InterceptorRegistry registry) {

        // Add the logging interceptor
        var loggingInterceptor = new LoggingInterceptor(this.context.getBeanFactory());
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns(ResourcePaths.ALL);

        // Add a custom header interceptor for testing failure scenarios
        var headerInterceptor = new CustomHeaderInterceptor(this.loggingConfiguration.getApiName());
        registry.addInterceptor(headerInterceptor)
                .addPathPatterns(ResourcePaths.ALL);
    }
}
