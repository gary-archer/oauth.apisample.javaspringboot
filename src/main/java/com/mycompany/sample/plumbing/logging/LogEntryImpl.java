package com.mycompany.sample.plumbing.logging;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import com.fasterxml.jackson.databind.JsonNode;
import com.kstruct.gethostname4j.Hostname;
import com.mycompany.sample.plumbing.claims.CoreApiClaims;
import com.mycompany.sample.plumbing.errors.ClientError;
import com.mycompany.sample.plumbing.errors.ServerError;

/*
 * Each API request writes a structured log entry containing fields we will query by
 * This class contains the behaviour and can be injected into business logic if required
 */
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class LogEntryImpl implements LogEntry {

    // The logger and its state
    private final Logger logger;
    private final Function<String, Integer> performanceThresholdCallback;
    private boolean started;
    private boolean finished;

    // Data logged
    private final LogEntryData data;
    private final ArrayList<LogEntryData> children;
    private LogEntryData activeChild;

    /*
     * The default constructor
     */
    public LogEntryImpl(final String apiName, final Logger logger) {
        this(apiName, logger, null);
    }

    /*
     * The main constructor
     */
    public LogEntryImpl(
            final String apiName,
            final Logger logger,
            final Function<String, Integer> performanceThresholdCallback) {

        // Store the logger and initialise state
        this.logger = logger;
        this.performanceThresholdCallback = performanceThresholdCallback;
        this.started = false;
        this.finished = false;

        // Initialise log data
        this.data = new LogEntryData();
        this.children = new ArrayList<>();
        this.activeChild = null;

        // Set initial fields
        this.data.setApiName(apiName);
        this.data.setHostName(Hostname.getHostname());
    }

    /*
     * Start collecting data before calling the API's business logic
     */
    public void start(
            final HttpServletRequest request,
            final RequestMappingHandlerMapping handlerMappings) {

        if (!this.started) {
            this.started = true;

            // Start measuring performance
            this.data.getPerformance().start();

            // Calculate fields from the request object
            this.data.setRequestVerb(request.getMethod());
            this.data.setRequestPath(this.getRequestPath(request));

            // Calculate the operation name
            this.setOperationName(request, handlerMappings);

            // Our callers can supply a custom header so that we can keep track of who is calling each API
            var callingApplicationName = request.getHeader("x-mycompany-api-client");
            if (!StringUtils.isEmpty(callingApplicationName)) {
                this.data.setClientApplicationName(callingApplicationName);
            }

            // Use the correlation id from request headers or create one
            var correlationId = request.getHeader("'x-mycompany-correlation-id");
            if (!StringUtils.isEmpty(correlationId)) {
                this.data.setCorrelationId(correlationId);
            } else {
                this.data.setCorrelationId(UUID.randomUUID().toString());
            }

            // Log an optional session id if supplied
            var sessionId = request.getHeader("x-mycompany-session-id");
            if (!StringUtils.isEmpty(sessionId)) {
                this.data.setSessionId(sessionId);
            }
        }
    }

    /*
     * Set the operation name from a string
     */
    public void setOperationName(final String operationName) {
        this.data.setOperationName(operationName);
    }

    /*
     * Add identity details for secured requests
     */
    public void setIdentity(final CoreApiClaims claims) {
        this.data.setClientOAuthId(claims.getClientId());
        this.data.setUserId(claims.getUserDatabaseId());
        this.data.setUserOAuthId(claims.getSubject());
    }

    /*
     * Create a child performance breakdown when requested
     */
    @Override
    public PerformanceBreakdown createPerformanceBreakdown(final String name) {
        return this.current().getPerformance().createChild(name);
    }

    /*
     * Set the resource id from the path segments, which is often a single id that is easy to query
     */
    public void setResourceId(final Map<String, String> pathVariables) {

        if (pathVariables != null) {
            this.data.setResourceId(String.join("/", pathVariables.values()));
        }
    }

    /*
     * Add a 5xx error to the log data
     */
    public void setServerError(final ServerError error) {
        this.current().setErrorData(error.toLogFormat(this.data.getApiName()));
        this.current().setErrorCode(error.getErrorCode());
        this.current().setErrorId(error.getInstanceId());
    }

    /*
     * Add a 4xx error to the log data
     */
    public void setClientError(final ClientError error) {
        this.current().setErrorData(error.toLogFormat());
        this.current().setErrorCode(error.getErrorCode());
    }

    /*
     * Enable free text to be added to production logs, though this should be avoided in most cases
     */
    @Override
    public void addInfo(final JsonNode info) {
        this.current().getInfoData().add(info);
    }

    /*
     * Start a child operation, which gets its own JSON log output
     */
    @Override
    public ChildLogEntry createChild(final String name) {

        // Fail if used incorrectly
        if (this.activeChild != null) {
            throw new IllegalStateException(
                    "The previous child operation must be completed before a new child can be started");
        }

        // Initialise the child
        this.activeChild = new LogEntryData();
        this.activeChild.setPerformanceThresholdMilliseconds(this.performanceThresholdCallback.apply(name));
        this.activeChild.setOperationName(name);
        this.activeChild.getPerformance().start();

        // Add to the parent and return an object to simplify disposal
        this.children.add(this.activeChild);
        return new ChildLogEntry(this);
    }

    /*
     * Finish collecting data at the end of the API request and write the output
     */
    public void end(final HttpServletResponse response) {

        if (!this.finished) {
            this.finished = true;

            // If an active child operation needs ending (due to exceptions) then we do it here
            this.endChildOperation();

            // Finish performance measurements
            this.data.getPerformance().close();

            // Calculate fields from the response object
            this.data.setStatusCode(response.getStatus());

            // Do normal finalisation, such as setting denormalised fields
            this.data.finalise();

            // Finalise data related to child log entries, to copy data points between parent and children
            for (var child : this.children) {
                child.finalise();
                child.updateFromParent(this.data);
                this.data.updateFromChild(child);
            }
        }
    }

    /*
     * Output any child data and then the parent data
     */
    public void write() {

        for (var child : this.children) {
            this.writeDataItem(child);
        }

        this.writeDataItem(this.data);
    }

    /*
     * Complete the active child operation
     */
    public void endChildOperation() {

        if (this.activeChild != null) {
            this.activeChild.getPerformance().close();
            this.activeChild = null;
        }
    }

    /*
     * Return the full request path and query string to be logged
     */
    private String getRequestPath(final HttpServletRequest request) {

        if (StringUtils.isEmpty(request.getQueryString())) {
            return request.getRequestURI();
        }

        return String.format("%s?%s", request.getRequestURI(), request.getQueryString());
    }

    /*
     * Calculate the operation name from metadata
     */
    private void setOperationName(
            final HttpServletRequest request,
            final RequestMappingHandlerMapping handlerMapping) {

        // Get metadata for all methods
        var methods = handlerMapping.getHandlerMethods();
        for (RequestMappingInfo mappingInfo : methods.keySet()) {

            // Find a match
            var match = mappingInfo.getMatchingCondition(request);
            if (match != null) {

                // Set the operation name
                var handlerMethod = methods.get(mappingInfo);
                this.data.setOperationName(handlerMethod.getMethod().getName());

                // Record the operation name and also ensure that the correct performance threshold is used
                this.data.setPerformanceThresholdMilliseconds(
                        this.performanceThresholdCallback.apply(this.data.getOperationName()));
            }
        }
    }

    /*
     * Get the data to use when a child operation needs to be managed
     */
    private LogEntryData current() {

        if (this.activeChild != null) {
            return this.activeChild;
        } else {
            return this.data;
        }
    }

    /*
     * Write a single data item by sending the object node to the BareJsonLoggingLayout
     */
    private void writeDataItem(final LogEntryData item) {

        if (this.data.isError()) {
            this.logger.error("error", item.toLogFormat());
        } else {
            this.logger.info("info", item.toLogFormat());
        }
    }
}
