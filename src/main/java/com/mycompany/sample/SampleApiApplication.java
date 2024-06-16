package com.authsamples.api;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import com.authsamples.api.host.startup.ApplicationInitializer;
import com.authsamples.api.plumbing.logging.LoggerFactoryBuilder;

/*
 * The application entry point
 */
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
@SpringBootApplication
public class SampleApiApplication {

    /*
     * The entry point method which starts the app
     */
    public static void main(final String[] args) {

        // Create the logger factory
        var loggerFactory = LoggerFactoryBuilder.create();

        try {

            // Run the app
            new SpringApplicationBuilder(SampleApiApplication.class)
                .initializers(new ApplicationInitializer(loggerFactory))
                .run(args);

        } catch (Throwable ex) {

            // Report startup errors
            loggerFactory.logStartupError(ex);
        }
    }
}
