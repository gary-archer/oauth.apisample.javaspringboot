package com.authsamples.api.plumbing.logging;

import org.slf4j.Logger;
import com.authsamples.api.plumbing.configuration.LoggingConfiguration;

/*
 * An interface to access loggers from application code
 */
public interface LoggerFactory {

    // Called to set up logging based on JSON configuration
    void configure(LoggingConfiguration configuration);

    // Handle startup errors
    void logStartupError(Throwable exception);

    // Get the fixed request logger
    Logger getRequestLogger();

    // Get the fixed audit logger
    Logger getAuditLogger();

    // Get a named debug logger
    Logger getDebugLogger(Class type);
}
