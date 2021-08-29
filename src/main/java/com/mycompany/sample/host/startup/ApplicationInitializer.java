package com.mycompany.sample.host.startup;

import java.net.MalformedURLException;
import java.net.URL;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;
import com.mycompany.sample.host.claims.SampleClaimsProvider;
import com.mycompany.sample.host.configuration.ApiConfiguration;
import com.mycompany.sample.host.configuration.Configuration;
import com.mycompany.sample.logic.utilities.JsonFileReader;
import com.mycompany.sample.plumbing.dependencies.BaseCompositionRoot;
import com.mycompany.sample.plumbing.dependencies.CustomRequestScope;
import com.mycompany.sample.plumbing.logging.LoggerFactory;

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
        var configuration = reader.readFile("api.config.json", Configuration.class).join();

        // Initialise logging from configuration settings
        loggerFactory.configure(configuration.getLogging());

        // Configure API listening details
        this.configurePort(configuration.getApi());
        this.configureHttpDebugging(configuration.getApi());
        this.configureSsl(configuration);

        // Register our custom scope
        var container = context.getBeanFactory();
        container.registerScope(CustomRequestScope.NAME, new CustomRequestScope());

        // Register common code dependencies
        new BaseCompositionRoot(container)
                .useOAuth(configuration.getOauth())
                .withClaimsProvider(new SampleClaimsProvider())
                .withLogging(configuration.getLogging(), loggerFactory)
                .register();

        // Register this app's specific dependencies
        container.registerSingleton("ApiConfiguration", configuration.getApi());
    }

    /*
     * Set the HTTP/S port from configuration
     */
    private void configurePort(final ApiConfiguration configuration) {
        System.setProperty("server.port", Integer.toString(configuration.getPort()));
    }

    /*
     * Set up HTTP debugging if enabled, which requires the HTTP proxy's root certificate to be trusted
     */
    private void configureHttpDebugging(final ApiConfiguration configuration) {

        if (configuration.isUseProxy()) {
            try {

                var url = new URL(configuration.getProxyUrl());
                System.setProperty("https.proxyHost", url.getHost());
                System.setProperty("https.proxyPort", String.valueOf(url.getPort()));

            } catch (MalformedURLException ex) {

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

            // Reference our SSL certificate details
            System.setProperty("server.ssl.key-store", configuration.getApi().getSslCertificateFileName());
            System.setProperty("server.ssl.key-store-password", configuration.getApi().getSslCertificatePassword());

            // Prevent TLS 1.3 errors under load, which show up in our load test otherwise
            // https://bugs.openjdk.java.net/browse/JDK-8241248
            System.setProperty("server.ssl.enabled-protocols", "TLSv1.2");
        }
    }
}
