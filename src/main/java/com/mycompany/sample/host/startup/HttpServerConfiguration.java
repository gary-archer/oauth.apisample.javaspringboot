package com.mycompany.sample.host.startup;

import com.mycompany.sample.host.configuration.ApiConfiguration;
import com.mycompany.sample.framework.api.base.configuration.FrameworkConfiguration;
import com.mycompany.sample.framework.api.base.logging.LoggerFactory;
import com.mycompany.sample.framework.api.base.startup.FrameworkBuilder;
import com.mycompany.sample.host.utilities.WebStaticContentFileResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
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
@Configuration
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class HttpServerConfiguration extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {

    private final ApiConfiguration apiConfiguration;
    private final FrameworkConfiguration frameworkConfiguration;
    private final OncePerRequestFilter authorizer;
    private final LoggerFactory loggerFactory;
    private final ConfigurableApplicationContext context;

    public HttpServerConfiguration(
            final ApiConfiguration apiConfiguration,
            final FrameworkConfiguration frameworkConfiguration,
            final @Qualifier("Authorizer") OncePerRequestFilter authorizer,
            final LoggerFactory loggerFactory,
            final ConfigurableApplicationContext context) {

        this.apiConfiguration = apiConfiguration;
        this.frameworkConfiguration = frameworkConfiguration;
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
                .antMatcher("/api/**")
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

        var builder = new FrameworkBuilder(
                this.context.getBeanFactory(),
                this.frameworkConfiguration,
                this.loggerFactory);
        builder.addInterceptors(registry);
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

        var registration = registry.addMapping("/api/**");
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
        var mobileRootLocation = "file:../authguidance.mobilesample.android/web";

        // Add the resolvers
        registry.addResourceHandler("**/*")
                .setCachePeriod(0)
                .addResourceLocations(webRootLocation, desktopRootLocation)
                .resourceChain(true)
                .addResolver(new WebStaticContentFileResolver(
                        webRootLocation,
                        loopbackRootLocation,
                        desktopRootLocation,
                        mobileRootLocation));
    }
}
