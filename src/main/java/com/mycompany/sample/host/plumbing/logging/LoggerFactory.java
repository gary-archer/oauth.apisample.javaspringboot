package com.mycompany.sample.host.plumbing.logging;

import com.mycompany.sample.host.plumbing.configuration.LoggingConfiguration;
import org.slf4j.Logger;

/*
 * An interface to access loggers from application code
 */
public interface LoggerFactory {

    // Called to set up logging based on JSON configuration
    void configure(LoggingConfiguration configuration);

    // Handle startup errors
    void logStartupError(Throwable exception);

    // Get a named debug text logger for a developer PC
    Logger getDevelopmentLogger(Class type);
}
