package com.mycompany.sample.plumbing.logging;

import java.util.ArrayList;
import org.slf4j.Logger;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.sample.plumbing.configuration.LoggingConfiguration;
import com.mycompany.sample.plumbing.errors.ErrorUtils;
import com.mycompany.sample.plumbing.errors.ServerError;

/*
 * A custom logger factory to wrap the default one and give us greater control over output
 */
public final class LoggerFactoryImpl implements LoggerFactory {

    // Invent a specific named logger, which we will use to implement our production logging design
    private static final String PRODUCTION_LOGGER_NAME = "PRODUCTION_LOGGER";

    private String apiName;
    private String developmentNamespace;
    private boolean isInitialized;
    private int defaultPerformanceThresholdMilliseconds;
    private final ArrayList<PerformanceThreshold> thresholdOverrides;

    /*
     * Set logging defaults when constructed
     */
    public LoggerFactoryImpl() {

        this.isInitialized = false;
        this.apiName = "";
        this.developmentNamespace = "";
        this.defaultPerformanceThresholdMilliseconds = 1000;
        this.thresholdOverrides = new ArrayList<>();
    }

    /*
     * Configure logging programmatically from our JSON configuration file
     */
    public void configure(final LoggingConfiguration configuration) {

        // Store the name, which will enable this API's logs to be distinguished from other APIs
        this.apiName = configuration.getApiName();

        // Initialise the production logger
        var prodConfiguration = configuration.getProduction();
        var prodLevelNode = prodConfiguration.get("level");
        var productionLevel = Level.toLevel(prodLevelNode.asText().toUpperCase(), Level.INFO);
        this.configureProductionLogger(productionLevel, prodConfiguration.get("appenders"));
        this.loadPerformanceThresholds(prodConfiguration);

        // Initialise any development loggers
        var devConfiguration = configuration.getDevelopment();
        this.configureDevelopmentLoggers(devConfiguration);

        // Indicate successful configuration
        this.isInitialized = true;
    }

    /*
     * Special handling for startup errors
     */
    @Override
    public void logStartupError(final Throwable exception) {

        // Create the logger if needed
        if (!this.isInitialized) {
            this.configureProductionLogger(Level.INFO, null);
        }

        // Get the error into a loggable format
        var error = (ServerError) ErrorUtils.fromException(exception);

        // Create a log entry and set error details
        var logEntry = new LogEntryImpl(this.apiName, this.getProductionLogger());
        logEntry.setOperationName("startup");
        logEntry.setServerError(error);
        logEntry.write();
    }

    /*
     * Get a logger per class for tracing debug statements on a developer PC
     */
    @Override
    public Logger getDevelopmentLogger(final Class type) {
        String loggerName = String.format("%s.%s", this.developmentNamespace, type.getSimpleName());
        return org.slf4j.LoggerFactory.getLogger(loggerName);
    }

    /*
     * Use the logging configuration to create a log entry the first time it is asked for during an API request
     */
    public LogEntryImpl createLogEntry() {

        return new LogEntryImpl(
                this.apiName,
                this.getProductionLogger(),
                this::getPerformanceThreshold);
    }

    /*
     * Create the production logger, which logs a bare JSON object
     */
    private void configureProductionLogger(final Level productionLevel, final JsonNode appendersConfig) {

        LoggerContext context = (LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();

        // Get the production logger and turn off inheritance so that it no longer uses text logging
        var logger = context.getLogger(PRODUCTION_LOGGER_NAME);
        logger.setAdditive(false);

        // Set the level from configuration
        logger.setLevel(productionLevel);

        // Add the console appender if required
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
        var devLevel = developmentLogConfig.get("level");
        var developmentLevel = Level.toLevel(devLevel.asText().toUpperCase(), Level.INFO);
        var rootLogger = context.getLogger(this.developmentNamespace);
        rootLogger.setLevel(developmentLevel);

        // Set override levels
        var overrideLevels = developmentLogConfig.get("overrideLevels");
        if (overrideLevels != null) {

            var fields = overrideLevels.fields();
            while (fields.hasNext()) {

                // Read the class name and log level
                var field = fields.next();
                var name = field.getKey();
                var level = Level.toLevel(field.getValue().asText().toUpperCase(), Level.INFO);

                // Add to our data
                String loggerName = String.format("%s.%s", this.developmentNamespace, name);
                var logger = context.getLogger(loggerName);
                logger.setLevel(level);
            }
        }
    }

    /*
     * Create a custom appender for outputting production log data to the console on a developer PC
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
        var layout = new BareJsonLoggingLayout(prettyPrint);
        layout.setContext(context);
        layout.start();

        // Create an appender that uses the layout
        var appender = new ConsoleAppender<ILoggingEvent>();
        appender.setContext(context);
        appender.setLayout(layout);
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
        final var defaultFileLimit = 100;
        final var defaultSizeLimit = 1024;
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

        // Configure its rolling policy
        var policy = new TimeBasedRollingPolicy<ILoggingEvent>();
        policy.setContext(context);
        policy.setParent(appender);
        policy.setFileNamePattern(filePattern);
        policy.setMaxHistory(maxFiles);

        // Set size details and complete the rolling policy
        var triggerPolicy = new SizeAndTimeBasedFNATP<ILoggingEvent>();
        triggerPolicy.setMaxFileSize(fileSize);
        policy.setTimeBasedFileNamingAndTriggeringPolicy(triggerPolicy);
        policy.start();

        // The log data is bare JSON without any logback fields
        // It uses a JSON object per line, which works better with log shippers
        var layout = new BareJsonLoggingLayout(false);
        layout.setContext(context);
        layout.start();

        // Set the policy against the appender
        appender.setRollingPolicy(policy);
        appender.setLayout(layout);
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

    /*
     * Extract performance details from the log configuration, for use later when creating log entries
     */
    private void loadPerformanceThresholds(final ObjectNode prodConfiguration) {

        // Read the default performance threshold
        var thresholds = prodConfiguration.get("performanceThresholdsMilliseconds");
        this.defaultPerformanceThresholdMilliseconds =
                thresholds.get("default").asInt(this.defaultPerformanceThresholdMilliseconds);

        // Set operation specific overrides
        var operationOverrides = thresholds.get("operationOverrides");
        if (operationOverrides != null) {

            var fields = operationOverrides.fields();
            while (fields.hasNext()) {

                // Read the operation name and threshold
                var field = fields.next();
                var name = field.getKey();
                var threshold = field.getValue().asInt(this.defaultPerformanceThresholdMilliseconds);

                // Add to our data
                this.thresholdOverrides.add(new PerformanceThreshold(name, threshold));
            }
        }
    }

    /*
     * Given an operation name, return its performance threshold
     */
    private int getPerformanceThreshold(final String name) {

        var found = this.thresholdOverrides.stream().filter(
                p -> p.getName().equalsIgnoreCase(name)).findFirst();

        if (found.isPresent()) {
            return found.get().getMilliseconds();
        }

        return this.defaultPerformanceThresholdMilliseconds;
    }
}
