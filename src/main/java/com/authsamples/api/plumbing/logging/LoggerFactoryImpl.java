package com.authsamples.api.plumbing.logging;

import org.slf4j.Logger;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import com.authsamples.api.plumbing.configuration.LoggingConfiguration;
import com.authsamples.api.plumbing.errors.ErrorUtils;
import com.authsamples.api.plumbing.errors.ServerError;
import com.fasterxml.jackson.databind.JsonNode;

/*
 * A custom logger factory to wrap the default one and give us greater control over output
 */
public final class LoggerFactoryImpl implements LoggerFactory {

    // Invent a specific named logger, which we will use to implement our production logging design
    private static final String PRODUCTION_LOGGER_NAME = "PRODUCTION_LOGGER";

    private String apiName;
    private int performanceThresholdMilliseconds;
    private String developmentNamespace;


    /*
     * Set logging defaults when constructed
     */
    public LoggerFactoryImpl() {

        this.apiName = "";
        this.performanceThresholdMilliseconds = 1000;
        this.developmentNamespace = "";
    }

    /*
     * Configure logging programmatically from our JSON configuration file
     */
    public void configure(final LoggingConfiguration configuration) {

        // Store the name, which will enable this API's logs to be distinguished from other APIs
        this.apiName = configuration.getApiName();

        /*
        // Initialise the production logger
        var prodConfiguration = configuration.getProduction();
        this.configureProductionLogger(prodConfiguration);

        // Initialise any development loggers
        var devConfiguration = configuration.getDevelopment();
        this.configureDevelopmentLoggers(devConfiguration);
        */
    }

    /*
     * Special handling for startup errors, where loggers may not be initialized yet
     */
    @Override
    public void logStartupError(final Throwable exception) {

        // Ensure that there is a production logger
        this.configureDefaultProductionLogger();

        // Get the error
        var error = (ServerError) ErrorUtils.fromException(exception);

        // Write it as a log entry
        var logEntry = new LogEntryImpl(this.apiName);
        logEntry.setOperationName("startup");
        logEntry.setServerError(error);
        // logEntry.write();
    }

    /*
     * Get the fixed request logger
     */
    @Override
    public Logger getRequestLogger() {
        return null;
    }

    /*
     * Get the fixed audit logger
     */
    @Override
    public Logger getAuditLogger() {
        return null;
    }

    /*
     * Get a logger per class for local debugging
     */
    @Override
    public Logger getDebugLogger(final Class type) {
        String loggerName = String.format("%s.%s", this.developmentNamespace, type.getSimpleName());
        return org.slf4j.LoggerFactory.getLogger(loggerName);
    }

    /*
     * Use the logging configuration to create a log entry the first time it is asked for during an API request
     */
    public LogEntryImpl createLogEntry() {
        return new LogEntryImpl(this.apiName, this.performanceThresholdMilliseconds);
    }

    /*
     * Create the production logger, which logs a bare JSON object
     */
    private void configureProductionLogger(final JsonNode productionLogConfig) {

        LoggerContext context = (LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();

        // Get the production logger and turn off inheritance so that it no longer uses text logging
        var logger = context.getLogger(PRODUCTION_LOGGER_NAME);
        logger.setAdditive(false);

        // Create the logger based on configuration
        var prodLevelNode = productionLogConfig.get("level");
        this.performanceThresholdMilliseconds = productionLogConfig.get("performanceThresholdMilliseconds").asInt();
        var prodLevel = Level.toLevel(prodLevelNode.asText().toUpperCase(), Level.INFO);
        logger.setLevel(prodLevel);

        // Add the console appender if required
        var appendersConfig = productionLogConfig.get("appenders");
        var consoleAppender = this.createProductionConsoleAppender(appendersConfig, context);
        if (consoleAppender != null) {
            logger.addAppender(consoleAppender);
        }

        // Add the file appender if required
        var fileAppender = this.createProductionFileAppender(appendersConfig, context);
        if (fileAppender != null) {
            logger.addAppender(fileAppender);
        }
    }

    /*
     * Create a logger for errors before the system has been initialized
     */
    private void configureDefaultProductionLogger() {

        // The logger is additive when the above method has not been called yet
        LoggerContext context = (LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();
        var logger = context.getLogger(PRODUCTION_LOGGER_NAME);
        if (logger.isAdditive()) {

            // Use a JSON encoder
            var encoder = new BareJsonEncoder(true);

            // Create an appender that uses the encoder
            var appender = new ConsoleAppender<ILoggingEvent>();
            appender.setContext(context);
            appender.setEncoder(encoder);
            appender.start();

            // Update the logger with the appender
            logger.setAdditive(false);
            logger.addAppender(appender);
        }
    }

    /*
     * Get the production logger
     */
    private Logger getProductionLogger() {
        return org.slf4j.LoggerFactory.getLogger(PRODUCTION_LOGGER_NAME);
    }

    /*
     * In our samples, developer loggers only output to the console
     */
    private void configureDevelopmentLoggers(final JsonNode developmentLogConfig) {

        // Get details
        LoggerContext context = (LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();

        // Set the namespace for development logging
        this.developmentNamespace = developmentLogConfig.get("namespace").asText();

        // Set the root log level, which will be the default for all loggers per class
        var devLevelNode = developmentLogConfig.get("level");
        var devLevel = Level.toLevel(devLevelNode.asText().toUpperCase(), Level.INFO);
        var rootLogger = context.getLogger(this.developmentNamespace);
        rootLogger.setLevel(devLevel);

        // Set override levels
        var overrideLevels = developmentLogConfig.get("overrideLevels");
        if (overrideLevels != null) {

            var properties = overrideLevels.properties();
            for (var property : properties) {

                // Read the class name and log level
                var name = property.getKey();
                var level = Level.toLevel(property.getValue().asText().toUpperCase(), Level.INFO);

                // Configure the logger
                String loggerName = String.format("%s.%s", this.developmentNamespace, name);
                var logger = context.getLogger(loggerName);
                logger.setLevel(level);
            }
        }
    }

    /*
     * Create a custom appender for outputting log data to the console on a developer PC
     */
    private ConsoleAppender<ILoggingEvent> createProductionConsoleAppender(
            final JsonNode appendersConfig,
            final LoggerContext context) {

        // If appenders are configured but no console appender then don't create one
        var consoleAppenderConfig = this.findAppenderByType(appendersConfig, "console");
        if (appendersConfig != null && consoleAppenderConfig == null) {
            return null;
        }

        // The log data is bare JSON without any logback fields, and can use pretty printing for readability
        var prettyPrint = consoleAppenderConfig.get("prettyPrint").asBoolean(false);
        var encoder = new BareJsonEncoder(prettyPrint);

        // Create an appender that uses the encoder
        var appender = new ConsoleAppender<ILoggingEvent>();
        appender.setContext(context);
        appender.setEncoder(encoder);
        appender.start();

        // Return it
        return appender;
    }

    /*
     * Create a custom appender that writes production logs as a single line of text, suitable for log shippers
     */
    private RollingFileAppender<ILoggingEvent> createProductionFileAppender(
            final JsonNode appendersConfig,
            final LoggerContext context) {

        // See if there is a file appender
        var fileAppenderConfig = this.findAppenderByType(appendersConfig, "file");
        if (fileAppenderConfig == null) {
            return null;
        }

        // Get settings
        var defaultFileLimit = 100;
        var defaultSizeLimit = 1024;
        var maxFiles = fileAppenderConfig.get("maxFiles").asInt(defaultFileLimit);
        var totalLogSizeMB = fileAppenderConfig.get("totalLogSizeMB").asInt(defaultSizeLimit);
        var filePrefix = fileAppenderConfig.get("filePrefix").asText();
        var logFolder = fileAppenderConfig.get("dirname").asText();

        // Set the file pattern to the date
        var filePattern = String.format("./%s/%s.%%d{yyyy-MM-dd}.%%i.log", logFolder, filePrefix);

        // Indicate the max size per file
        var fileSize = new FileSize(totalLogSizeMB * FileSize.MB_COEFFICIENT);

        // Create an appender that uses the config settings
        var appender = new RollingFileAppender<ILoggingEvent>();
        appender.setContext(context);

        // Configure the size and time for log files
        var policy = new SizeAndTimeBasedRollingPolicy<ILoggingEvent>();
        policy.setContext(context);
        policy.setParent(appender);
        policy.setFileNamePattern(filePattern);
        policy.setMaxHistory(maxFiles);
        policy.setMaxFileSize(fileSize);
        policy.start();

        // The log data uses a JSON object per line to support integration with log shippers
        var encoder = new BareJsonEncoder(false);

        // Set appender details
        appender.setRollingPolicy(policy);
        appender.setEncoder(encoder);
        appender.start();

        // Return it
        return appender;
    }

    /*
     * Find an appender and return its configuration section if found
     */
    private JsonNode findAppenderByType(final JsonNode appendersConfig, final String typeToFind) {

        if (appendersConfig == null) {
            return null;
        }

        var appenders = appendersConfig.elements();
        while (appenders.hasNext()) {

            var appender = appenders.next();
            var type = appender.get("type").asText();
            if (type.equals(typeToFind)) {
                return appender;
            }
        }

        return null;
    }
}
