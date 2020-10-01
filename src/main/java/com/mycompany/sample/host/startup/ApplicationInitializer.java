package com.mycompany.sample.host.startup;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import com.mycompany.sample.host.claims.SampleApiClaims;
import com.mycompany.sample.host.claims.SampleApiClaimsProvider;
import com.mycompany.sample.host.configuration.ApiConfiguration;
import com.mycompany.sample.host.configuration.Configuration;
import com.mycompany.sample.logic.utilities.JsonFileReader;
import com.mycompany.sample.plumbing.dependencies.BaseCompositionRoot;
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
    @SuppressWarnings("unchecked")
    public void initialize(final ConfigurableApplicationContext context) {

        // First load our configuration file
        var reader = new JsonFileReader();
        var configuration = reader.readFile("api.config.json", Configuration.class).join();

        // Initialise logging from configuration settings
        loggerFactory.configure(configuration.getLogging());

        // Configure the API to listen on SSL and to support proxying requests via an HTTP debugger
        this.configureHttpDebugging(configuration.getApi());
        this.configureSsl(context, configuration);

        // Register common code dependencies
        var container = context.getBeanFactory();
        new BaseCompositionRoot<SampleApiClaims>(container)
                .useApiBasePath("/api/")
                .useDiagnostics(configuration.getLogging(), loggerFactory)
                .useOAuth(configuration.getOauth())
                .useClaimsCaching(configuration.getClaims())
                .withClaimsSupplier(SampleApiClaims::new)
                .withCustomClaimsProviderSupplier(SampleApiClaimsProvider::new)
                .register();

        // Register this app's specific dependencies
        container.registerSingleton("ApiConfiguration", configuration.getApi());
    }

    /*
     * Set up HTTP debugging if enabled, which requires the HTTP debugger's root certificate to be trusted
     */
    private void configureHttpDebugging(final ApiConfiguration configuration) {

        if (configuration.isUseProxy()) {
            try {

                var url = new URL(configuration.getProxyUrl());
                System.setProperty("https.proxyHost", url.getHost());
                System.setProperty("https.proxyPort", String.valueOf(url.getPort()));

            } catch (MalformedURLException ex) {

                var message = String.format("Unable to parse proxy URL %s", configuration.getProxyUrl());
                throw new RuntimeException(message, ex);
            }
        }
    }

    /*
     * Configure the port to listen on and SSL properties
     */
    private void configureSsl(final ConfigurableApplicationContext context, final Configuration configuration) {

        // Set Spring Boot SSL related properties
        Map<String, Object> map = new HashMap<>();
        map.put("server.port", configuration.getApi().getSslPort());
        map.put("server.ssl.key-store", String.format("certs/%s", configuration.getApi().getSslCertificateFileName()));
        map.put("server.ssl.key-store-password", configuration.getApi().getSslCertificatePassword());

        // Set properties against the environment
        var propertySources = context.getEnvironment().getPropertySources();
        propertySources.addFirst(new MapPropertySource("runtimeProperties", map));
    }
}
