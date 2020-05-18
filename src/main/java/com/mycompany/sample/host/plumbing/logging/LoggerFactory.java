package com.mycompany.sample.host.plumbing.logging;

import com.mycompany.sample.host.configuration.FrameworkConfiguration;
import org.slf4j.Logger;

/*
 * An interface to access loggers
 */
public interface LoggerFactory {

    // Called to set up logging based on JSON configuration
    void configure(FrameworkConfiguration configuration);

    // Handle startup errors
    void logStartupError(Throwable exception);

    // Get a named debug text logger for a developer PC
    Logger getDevelopmentLogger(Class type);
}
