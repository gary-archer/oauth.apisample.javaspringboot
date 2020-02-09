package com.mycompany.sample.framework.api.base.logging;

import com.mycompany.sample.framework.api.base.configuration.FrameworkConfiguration;
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
