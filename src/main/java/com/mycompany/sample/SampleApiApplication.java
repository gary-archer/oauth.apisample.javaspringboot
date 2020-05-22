package com.mycompany.sample;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import com.ea.async.Async;
import com.mycompany.sample.host.startup.ApplicationInitializer;
import com.mycompany.sample.plumbing.logging.LoggerFactoryBuilder;

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

        // Initialise the EA library, which allows us to use async await syntax
        Async.init();

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
