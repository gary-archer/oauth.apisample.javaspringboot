package com.mycompany.api.basicapi.startup;

import com.mycompany.api.basicapi.configuration.AppConfiguration;
import com.mycompany.api.basicapi.configuration.Configuration;
import com.mycompany.api.basicapi.plumbing.utilities.ConfigurationProvider;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/*
 * Override startup to customize behaviour
 */
public class ApplicationInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>
{
    /*
     * Set properties from our JSON configuration file
     */
    @Override
    public void initialize(ConfigurableApplicationContext context) {

        // Load our configuration
        var configuration = new ConfigurationProvider().Load();

        // Configure the port to listen on and SSL properties
        Map<String, Object> map = new HashMap<>();
        map.put("server.port", this.getSslPortNumber(configuration));
        map.put("server.ssl.key-store", String.format("certs/%s", configuration.getApp().sslCertificateFileName));
        map.put("server.ssl.key-store-password", configuration.getApp().sslCertificatePassword);

        // Also enable HTTP debugging if required
        this.configureHttpDebugging(configuration.getApp());

        // Set properties against the environment
        var propertySources = context.getEnvironment().getPropertySources();
        propertySources.addFirst(new MapPropertySource("runtimeProperties", map));

        // Also register the configuration as a singleton so that we can inject it into other classes
        context.getBeanFactory().registerSingleton("JsonConfiguration", configuration);
    }

    /*
     * Calculate the port number from the web trusted origin
     */
    private int getSslPortNumber(Configuration configuration)
    {
        try {
            var port = new URL(configuration.getApp().getTrustedOrigins()[0]).getPort();
            if(port <= 0)
            {
                return 443;
            }

            return port;
        }
        catch(MalformedURLException ex)
        {
            var message = String.format("Unable to parse trusted origin URL %s", configuration.getApp().getTrustedOrigins()[0]);
            throw new RuntimeException(message, ex);
        }
    }

    /*
     * Set up HTTP debugging if enabled, which requires SSL trust to be configured as a prerequisite
     * On my Windows PC the trust store is at this location:
     *   C:\Program Files\Java\jdk-11\lib\security\cacerts'
     * I had to run the following command against the above file:
     *   keytool -keystore cacerts -importcert -alias FiddlerRoot  -file FiddlerRoot.crt
     */
    private void configureHttpDebugging(AppConfiguration configuration) {

        try {
            if (configuration.useProxy) {

                var url = new URL(configuration.proxyUrl);
                System.setProperty("https.proxyHost", url.getHost());
                System.setProperty("https.proxyPort", String.valueOf(url.getPort()));
            }
        }
        catch(MalformedURLException ex)
        {
            var message = String.format("Unable to parse proxy URL %s", configuration.proxyUrl);
            throw new RuntimeException(message, ex);
        }
    }
}
