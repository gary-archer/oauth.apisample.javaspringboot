package com.authsamples.api.host.startup;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;
import com.authsamples.api.host.configuration.ApiConfiguration;
import com.authsamples.api.host.configuration.Configuration;
import com.authsamples.api.host.dependencies.CompositionRoot;
import com.authsamples.api.logic.claims.ExtraClaimsProviderImpl;
import com.authsamples.api.logic.utilities.JsonFileReader;
import com.authsamples.api.plumbing.dependencies.CustomRequestScope;
import com.authsamples.api.plumbing.logging.LoggerFactory;

/*
 * Override startup to customize behaviour
 */
public final class ApplicationInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private final LoggerFactory loggerFactory;

    /*
     * Receive the logger at startup
     */
    public ApplicationInitializer(final LoggerFactory loggerFactory) {
        this.loggerFactory = loggerFactory;
    }

    /*
     * Set properties from our JSON configuration file
     */
    @Override
    public void initialize(final ConfigurableApplicationContext context) {

        // First load our configuration file
        var reader = new JsonFileReader();
        var configuration = reader.readFile("api.config.json", Configuration.class);

        // Initialise logging from configuration settings
        loggerFactory.configure(configuration.getLogging());

        // Configure API listening details
        this.configureHttpListener(configuration.getApi());
        this.configureHttpDebugging(configuration.getApi());
        this.configureSsl(configuration);

        // Register our custom scope
        var container = context.getBeanFactory();
        container.registerScope(CustomRequestScope.NAME, new CustomRequestScope());

        // Register dependencies with the container
        new CompositionRoot(container)
                .addConfiguration(configuration)
                .addLogging(loggerFactory)
                .addExtraClaimsProvider(new ExtraClaimsProviderImpl(container))
                .register();
    }

    /*
     * Set the port from configuration and activate HTTP/2 if we run locally
     * In real deployments like Kubernetes the API uses HTTP and can use a service mesh for TLS
     */
    private void configureHttpListener(final ApiConfiguration configuration) {

        System.setProperty("server.port", Integer.toString(configuration.getPort()));
        if (StringUtils.hasLength(configuration.getSslCertificateFileName())
                && StringUtils.hasLength(configuration.getSslCertificatePassword())) {

            System.setProperty("server.http2.enabled", "true");
        }
    }

    /*
     * Set up HTTP debugging if enabled, which requires the HTTP proxy's root certificate to be trusted
     */
    private void configureHttpDebugging(final ApiConfiguration configuration) {

        if (configuration.isUseProxy()) {
            try {

                var url = new URI(configuration.getProxyUrl()).toURL();
                System.setProperty("https.proxyHost", url.getHost());
                System.setProperty("https.proxyPort", String.valueOf(url.getPort()));

            } catch (MalformedURLException | URISyntaxException ex) {

                var message = String.format("Unable to parse proxy URL %s", configuration.getProxyUrl());
                throw new IllegalStateException(message, ex);
            }
        }
    }

    /*
     * Configure SSL certificate details
     */
    private void configureSsl(final Configuration configuration) {

        if (StringUtils.hasLength(configuration.getApi().getSslCertificateFileName())
                && StringUtils.hasLength(configuration.getApi().getSslCertificatePassword())) {

            System.setProperty("server.ssl.key-store", configuration.getApi().getSslCertificateFileName());
            System.setProperty("server.ssl.key-store-password", configuration.getApi().getSslCertificatePassword());
        }
    }
}
