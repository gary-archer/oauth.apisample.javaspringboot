package com.mycompany.sample.host.startup;

import com.mycompany.sample.host.configuration.ApiConfiguration;
import com.mycompany.sample.host.configuration.Configuration;
import com.mycompany.sample.host.plumbing.configuration.LoggingConfiguration;
import com.mycompany.sample.host.plumbing.interceptors.CustomHeaderInterceptor;
import com.mycompany.sample.host.plumbing.interceptors.LoggingInterceptor;
import com.mycompany.sample.host.plumbing.logging.LoggerFactory;
import com.mycompany.sample.host.utilities.WebStaticContentFileResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
 * A class to manage HTTP configuration for our server
 */
@org.springframework.context.annotation.Configuration
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class HttpServerConfiguration extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {

    // Injected properties
    private final ApiConfiguration apiConfiguration;
    private final LoggingConfiguration loggingConfiguration;
    private final OncePerRequestFilter authorizer;
    private final LoggerFactory loggerFactory;
    private final ConfigurableApplicationContext context;

    // Constants
    private final String apiRequestPaths = "/api/**";

    /*
     * Construction via the container
     */
    public HttpServerConfiguration(
            final ApiConfiguration apiConfiguration,
            final LoggingConfiguration loggingConfiguration,
            final @Qualifier("Authorizer") OncePerRequestFilter authorizer,
            final LoggerFactory loggerFactory,
            final ConfigurableApplicationContext context) {

        this.apiConfiguration = apiConfiguration;
        this.loggingConfiguration = loggingConfiguration;
        this.authorizer = authorizer;
        this.loggerFactory = loggerFactory;
        this.context = context;
    }

    /*
     * Configure how API requests are secured
     * We use a OncePerRequestFilter as opposed to providing a custom ResourceServerTokenServices
     * The latter fires again when a CompletableFuture moves to the ASYNC / completed stage
     */
    @Override
    public void configure(final HttpSecurity http) throws Exception {

        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .antMatcher(this.apiRequestPaths)
                .authorizeRequests()
                .anyRequest()
                .authenticated()
                .and()
                .addFilterBefore(this.authorizer, AbstractPreAuthenticatedProcessingFilter.class);
    }

    /*
     * Configure cross cutting concerns
     */
    @Override
    public void addInterceptors(final InterceptorRegistry registry) {

        // Add the logging interceptor for API requests
        var loggingInterceptor = new LoggingInterceptor(this.context.getBeanFactory());
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns(this.apiRequestPaths);

        // Add a custom header interceptor for testing failure scenarios
        var headerInterceptor = new CustomHeaderInterceptor(
                this.context.getBeanFactory(),
                this.loggingConfiguration.getApiName());
        registry.addInterceptor(headerInterceptor);
    }

    /*
     * Ensure that OPTIONS requests are not passed to the authorizer
     */
    @Override
    public void configure(final WebSecurity web) {
        web.ignoring().antMatchers(HttpMethod.OPTIONS, "/api/**");
    }

    /*
     * Configure our API to allow cross origin requests from our SPA
     */
    @Override
    public void addCorsMappings(final CorsRegistry registry) {

        var registration = registry.addMapping(this.apiRequestPaths);
        var trustedOrigins = this.apiConfiguration.getTrustedOrigins();
        for (var trustedOrigin: trustedOrigins) {
            registration.allowedOrigins(trustedOrigin);
        }
    }

    /*
     * For demo purposes this sample also serves static content for UIs
     */
    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {

        // Projects that own static content should be built in a parallel folder to this API
        var webRootLocation = "file:../authguidance.websample.final/spa";
        var loopbackRootLocation = "file:../authguidance.desktopsample1/web";
        var desktopRootLocation = "file:../authguidance.desktopsample.final/web";
        var androidRootLocation = "file:../authguidance.mobilesample.android/web";
        var iosRootLocation = "file:../authguidance.mobilesample.ios/web";

        // Add the resolvers
        registry.addResourceHandler("**/*")
                .setCachePeriod(0)
                .addResourceLocations(webRootLocation, desktopRootLocation)
                .resourceChain(true)
                .addResolver(new WebStaticContentFileResolver(
                        webRootLocation,
                        loopbackRootLocation,
                        desktopRootLocation,
                        androidRootLocation,
                        iosRootLocation));
    }
}
