package com.mycompany.sample.host.startup;

import java.net.MalformedURLException;
import java.net.URL;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;
import com.mycompany.sample.host.claims.SampleCustomClaimsProvider;
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

    private final LoggerFactory _loggerFactory;

    /*
     * Receive the logger at startup
     */
    public ApplicationInitializer(final LoggerFactory loggerFactory) {
        this._loggerFactory = loggerFactory;
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
        _loggerFactory.configure(configuration.get_logging());

        // Configure API listening details
        this.configurePort(configuration.get_api());
        this.configureHttpDebugging(configuration.get_api());
        this.configureSsl(configuration);

        // Register our custom scope
        var container = context.getBeanFactory();
        container.registerScope(CustomRequestScope.NAME, new CustomRequestScope());

        // Register common code dependencies
        new BaseCompositionRoot(container)
                .useOAuth(configuration.get_oauth())
                .withCustomClaimsProvider(new SampleCustomClaimsProvider())
                .withLogging(configuration.get_logging(), _loggerFactory)
                .register();

        // Register this app's specific dependencies
        container.registerSingleton("ApiConfiguration", configuration.get_api());
    }

    /*
     * Set the HTTP/S port from configuration
     */
    private void configurePort(final ApiConfiguration configuration) {
        System.setProperty("server.port", Integer.toString(configuration.get_port()));
    }

    /*
     * Set up HTTP debugging if enabled, which requires the HTTP proxy's root certificate to be trusted
     */
    private void configureHttpDebugging(final ApiConfiguration configuration) {

        if (configuration.is_useProxy()) {
            try {

                var url = new URL(configuration.get_proxyUrl());
                System.setProperty("https.proxyHost", url.getHost());
                System.setProperty("https.proxyPort", String.valueOf(url.getPort()));

            } catch (MalformedURLException ex) {

                var message = String.format("Unable to parse proxy URL %s", configuration.get_proxyUrl());
                throw new IllegalStateException(message, ex);
            }
        }
    }

    /*
     * Configure SSL certificate details
     */
    private void configureSsl(final Configuration configuration) {

        if (StringUtils.hasLength(configuration.get_api().get_sslCertificateFileName())
                && StringUtils.hasLength(configuration.get_api().get_sslCertificatePassword())) {

            // Reference our SSL certificate details
            System.setProperty("server.ssl.key-store", configuration.get_api().get_sslCertificateFileName());
            System.setProperty("server.ssl.key-store-password", configuration.get_api().get_sslCertificatePassword());

            // Prevent TLS 1.3 errors under load, which show up in our load test otherwise
            // https://bugs.openjdk.java.net/browse/JDK-8241248
            System.setProperty("server.ssl.enabled-protocols", "TLSv1.2");
        }
    }
}
