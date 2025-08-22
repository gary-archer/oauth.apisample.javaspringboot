package com.authsamples.api.plumbing.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
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
import com.fasterxml.jackson.databind.node.ArrayNode;

/*
 * A custom logger factory to wrap the default one and give us greater control over output
 */
public final class LoggerFactoryImpl implements LoggerFactory {

    private String apiName;
    private int performanceThresholdMilliseconds;
    private String developmentNamespace;
    private boolean hasRequestLogger;
    private boolean hasAuditLogger;

    /*
     * Set logging defaults when constructed
     */
    public LoggerFactoryImpl() {

        this.apiName = "";
        this.performanceThresholdMilliseconds = 1000;
        this.developmentNamespace = "";
        this.hasRequestLogger = false;
        this.hasAuditLogger = false;
    }

    /*
     * Configure logging programmatically from our JSON configuration file
     */
    public void configure(final LoggingConfiguration configuration) {

        this.apiName = configuration.getApiName();

        // Create the fixed request logger
        var requestLogConfig = this.findArrayElementByType(configuration.getLoggers(), "request");
        if (requestLogConfig != null) {
            this.createRequestLogger(requestLogConfig);
        }

        // Create the fixed audit logger
        var auditLogConfig = this.findArrayElementByType(configuration.getLoggers(), "audit");
        if (auditLogConfig != null) {
            this.createAuditLogger(auditLogConfig);
        }

        var debugLogConfig = this.findArrayElementByType(configuration.getLoggers(), "debug");
        if (debugLogConfig != null) {
            this.createDebugLoggers(debugLogConfig);
        }
    }

    /*
     * Special handling for startup errors, where loggers may not be initialized yet
     */
    @Override
    public void logStartupError(final Throwable exception) {

        // Create a request logger if required
        var startupLogger = this.getStartupLogger();

        // Get the error
        var error = (ServerError) ErrorUtils.fromException(exception);

        // Write it as a log entry
        var logEntry = new LogEntryImpl(this.apiName);
        logEntry.setOperationName("startup");
        logEntry.setServerError(error);

        // Output the data
        startupLogger.info("info", logEntry.getRequestLog());
    }

    /*
     * Get the fixed request logger
     */
    @Override
    public org.slf4j.Logger getRequestLogger() {

        if (this.hasRequestLogger) {
            return org.slf4j.LoggerFactory.getLogger("request");
        }

        return null;
    }

    /*
     * Get the fixed audit logger
     */
    @Override
    public org.slf4j.Logger getAuditLogger() {

        if (this.hasAuditLogger) {
            return org.slf4j.LoggerFactory.getLogger("audit");
        }

        return null;
    }

    /*
     * Get a logger per class for local debugging
     */
    @Override
    public org.slf4j.Logger getDebugLogger(final Class type) {
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
     * Add an always on request logger for technical support details
     */
    private void createRequestLogger(final JsonNode requestLogConfig) {

        LoggerContext context = (LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();

        // Get the logger and disable the default text logging
        var logger = context.getLogger("request");
        logger.setAdditive(false);
        logger.setLevel(Level.INFO);
        this.performanceThresholdMilliseconds = requestLogConfig.get("performanceThresholdMilliseconds").asInt();

        // Add appenders to the logger
        createAppenders(logger, context, requestLogConfig.get("appenders"));
        this.hasRequestLogger = true;
    }

    /*
     * Add an always on audit logger for technical support details
     */
    private void createAuditLogger(final JsonNode auditLogConfig) {

        LoggerContext context = (LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();

        // Get the logger and disable the default text logging
        var logger = context.getLogger("audit");
        logger.setAdditive(false);
        logger.setLevel(Level.INFO);

        // Add appenders to the logger
        createAppenders(logger, context, auditLogConfig.get("appenders"));
        this.hasAuditLogger = true;
    }

    /*
     * Add appenders from configuration
     */
    private void createAppenders(
            final Logger logger,
            final LoggerContext context,
            final JsonNode appendersConfig) {

        // Add the console appender if required
        if (appendersConfig != null && appendersConfig.isArray()) {

            var consoleConfig = this.findArrayElementByType((ArrayNode) appendersConfig, "console");
            if (consoleConfig != null) {
                var consoleAppender = this.createConsoleAppender(consoleConfig, context);
                logger.addAppender(consoleAppender);
            }

            var fileConfig = this.findArrayElementByType((ArrayNode) appendersConfig, "file");
            if (fileConfig != null) {
                var fileAppender = this.createFileAppender(fileConfig, context);
                logger.addAppender(fileAppender);
            }
        }
    }

    /*
     * Create a JSON console appender
     */
    private ConsoleAppender<ILoggingEvent> createConsoleAppender(
            final JsonNode config,
            final LoggerContext context) {

        // The log data is bare JSON without any logback fields, and can use pretty printing for readability
        var prettyPrint = config.get("prettyPrint").asBoolean(false);
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
     * Create a JSON file appender
     */
    private RollingFileAppender<ILoggingEvent> createFileAppender(
            final JsonNode config,
            final LoggerContext context) {

        // Get settings
        var defaultFileLimit = 100;
        var defaultSizeLimit = 1024;
        var maxFiles = config.get("maxFiles").asInt(defaultFileLimit);
        var totalLogSizeMB = config.get("totalLogSizeMB").asInt(defaultSizeLimit);
        var filePrefix = config.get("filePrefix").asText();
        var logFolder = config.get("dirname").asText();

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
     * Do an object array lookup
     */
    private JsonNode findArrayElementByType(final ArrayNode array, final String key) {

        var items = array.elements();
        while (items.hasNext()) {

            var item = items.next();
            var type = item.get("type").asText();
            if (type.equals(key)) {
                return item;
            }
        }

        return null;
    }

    /*
     * Create a default logger for startup errors if required
     */
    private org.slf4j.Logger getStartupLogger() {

        // Use the request logger if possible
        if (this.hasRequestLogger) {
            return this.getRequestLogger();
        }

        // Otherwise create a default request logger
        LoggerContext context = (LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();
        var logger = context.getLogger("request");
        var encoder = new BareJsonEncoder(true);

        // Create an appender that uses the encoder
        var appender = new ConsoleAppender<ILoggingEvent>();
        appender.setContext(context);
        appender.setEncoder(encoder);
        appender.start();

        // Update the logger with the appender
        logger.setAdditive(false);
        logger.addAppender(appender);
        return logger;
    }

    /*
     * The esxample's debug loggers output to the console as plain text
     */
    private void createDebugLoggers(final JsonNode debugLogConfig) {

        // Get details
        LoggerContext context = (LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();

        // Set the namespace for development logging
        this.developmentNamespace = debugLogConfig.get("namespace").asText();

        // Set the root log level, which will be the default for all loggers per class
        var devLevelNode = debugLogConfig.get("level");
        var devLevel = Level.toLevel(devLevelNode.asText().toUpperCase(), Level.INFO);
        var rootLogger = context.getLogger(this.developmentNamespace);
        rootLogger.setLevel(devLevel);

        // Set override levels
        var overrideLevels = debugLogConfig.get("overrideLevels");
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
}
