package com.mycompany.api.basicapi.plumbing.startup;

import com.mycompany.api.basicapi.configuration.Configuration;
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
        Configuration configuration = new ConfigurationProvider().Load();

        // Define our application properties
        Map<String, Object> map = new HashMap<>();
        map.put("server.port", this.getSslPortNumber(configuration));
        map.put("server.ssl.key-store", String.format("certs/%s", configuration.app.sslCertificateFileName));
        map.put("server.ssl.key-store-password", configuration.app.sslCertificatePassword);

        // Set the properties against the environment
        MutablePropertySources propertySources = context.getEnvironment().getPropertySources();
        propertySources.addFirst(new MapPropertySource("runtimeProperties", map));

        // Also register the configuration as a singleton so that we can inject it into other classes
        context.getBeanFactory().registerSingleton("JsonConfiguration", configuration);
    }

    /*
     * Calculate the port number from the web trusted origin
     */
    private Integer getSslPortNumber(Configuration configuration)
    {
        try {
            Integer port = new URL(configuration.app.trustedOrigins[0]).getPort();
            if(port <= 0)
            {
                return 443;
            }

            return port;
        }
        catch(MalformedURLException ex)
        {
            String message = String.format("Unable to parse port number from trusted origin URL %s", configuration.app.trustedOrigins[0]);
            throw new RuntimeException(message, ex);
        }
    }
}