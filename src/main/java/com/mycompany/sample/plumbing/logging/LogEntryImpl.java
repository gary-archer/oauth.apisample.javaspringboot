package com.mycompany.sample.plumbing.logging;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import com.fasterxml.jackson.databind.JsonNode;
import com.kstruct.gethostname4j.Hostname;
import com.mycompany.sample.plumbing.claims.BaseClaims;
import com.mycompany.sample.plumbing.errors.ClientError;
import com.mycompany.sample.plumbing.errors.ServerError;

/*
 * Each API request writes a structured log entry containing fields we will query by
 * This class contains the behaviour and can be injected into business logic if required
 */
public final class LogEntryImpl implements LogEntry {

    // The logger and its state
    private final Logger _logger;
    private final Function<String, Integer> _performanceThresholdCallback;
    private boolean _started;
    private boolean _finished;

    // Data logged
    private final LogEntryData _data;
    private final ArrayList<LogEntryData> _children;
    private LogEntryData _activeChild;

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
        this._logger = logger;
        this._performanceThresholdCallback = performanceThresholdCallback;
        this._started = false;
        this._finished = false;

        // Initialise log data
        this._data = new LogEntryData();
        this._children = new ArrayList<>();
        this._activeChild = null;

        // Set initial fields
        this._data.set_apiName(apiName);
        this._data.set_hostName(Hostname.getHostname());
    }

    /*
     * Start collecting data before calling the API's business logic
     */
    public void start(final HttpServletRequest request) {

        if (!this._started) {
            this._started = true;

            // Start measuring performance
            this._data.get_performance().start();

            // Calculate fields from the request object
            this._data.set_requestVerb(request.getMethod());
            this._data.set_requestPath(this.getRequestPath(request));

            // Our callers can supply a custom header so that we can keep track of who is calling each API
            var callingApplicationName = request.getHeader("x-mycompany-api-client");
            if (StringUtils.hasLength(callingApplicationName)) {
                this._data.set_clientApplicationName(callingApplicationName);
            }

            // Use the correlation id from request headers or create one
            var correlationId = request.getHeader("'x-mycompany-correlation-id");
            if (StringUtils.hasLength(correlationId)) {
                this._data.set_correlationId(correlationId);
            } else {
                this._data.set_correlationId(UUID.randomUUID().toString());
            }

            // Log an optional session id if supplied
            var sessionId = request.getHeader("x-mycompany-session-id");
            if (StringUtils.hasLength(sessionId)) {
                this._data.set_sessionId(sessionId);
            }
        }
    }

    /*
     * Set the operation name from a string
     */
    public void setOperationName(final String operationName) {
        this._data.set_operationName(operationName);
    }

    /*
     * Add identity details for secured requests
     */
    public void setIdentity(final BaseClaims claims) {
        this._data.set_userOAuthId(claims.get_subject());
    }

    /*
     * Create a child performance breakdown when requested
     */
    @Override
    public PerformanceBreakdown createPerformanceBreakdown(final String name) {
        return this.current().get_performance().createChild(name);
    }

    /*
     * Set the resource id from the path segments, which is often a single id that is easy to query
     */
    public void setResourceId(final Map<String, String> pathVariables) {

        if (pathVariables != null) {
            this._data.set_resourceId(String.join("/", pathVariables.values()));
        }
    }

    /*
     * Add a 5xx error to the log data
     */
    public void setServerError(final ServerError error) {
        this.current().set_errorData(error.toLogFormat(this._data.get_apiName()));
        this.current().set_errorCode(error.getErrorCode());
        this.current().set_errorId(error.getInstanceId());
    }

    /*
     * Add a 4xx error to the log data
     */
    public void setClientError(final ClientError error) {
        this.current().set_errorData(error.toLogFormat());
        this.current().set_errorCode(error.getErrorCode());
    }

    /*
     * Enable free text to be added to production logs, though this should be avoided in most cases
     */
    @Override
    public void addInfo(final JsonNode info) {
        this.current().get_infoData().add(info);
    }

    /*
     * Start a child operation, which gets its own JSON log output
     */
    @Override
    public ChildLogEntry createChild(final String name) {

        // Fail if used incorrectly
        if (this._activeChild != null) {
            throw new IllegalStateException(
                    "The previous child operation must be completed before a new child can be started");
        }

        // Initialise the child
        this._activeChild = new LogEntryData();
        this._activeChild.set_performanceThresholdMilliseconds(this._performanceThresholdCallback.apply(name));
        this._activeChild.set_operationName(name);
        this._activeChild.get_performance().start();

        // Add to the parent and return an object to simplify disposal
        this._children.add(this._activeChild);
        return new ChildLogEntry(this);
    }

    /*
     * Finish collecting data at the end of the API request and write the output
     */
    public void end(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final RequestMappingHandlerMapping handlerMapping) {

        if (!this._finished) {
            this._finished = true;

            // Set details that are not available at the start of a request
            this.setOperationName(request, handlerMapping);

            // If an active child operation needs ending (due to exceptions) then we do it here
            this.endChildOperation();

            // Finish performance measurements
            this._data.get_performance().close();

            // Calculate fields from the response object
            this._data.set_statusCode(response.getStatus());

            // Do normal finalisation, such as setting denormalised fields
            this._data.finalise();

            // Finalise data related to child log entries, to copy data points between parent and children
            for (var child : this._children) {
                child.finalise();
                child.updateFromParent(this._data);
                this._data.updateFromChild(child);
            }
        }
    }

    /*
     * Output any child data and then the parent data
     */
    public void write() {

        for (var child : this._children) {
            this.writeDataItem(child);
        }

        this.writeDataItem(this._data);
    }

    /*
     * Complete the active child operation
     */
    public void endChildOperation() {

        if (this._activeChild != null) {
            this._activeChild.get_performance().close();
            this._activeChild = null;
        }
    }

    /*
     * Return the full request path and query string to be logged
     */
    private String getRequestPath(final HttpServletRequest request) {

        if (!StringUtils.hasLength(request.getQueryString())) {
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

        var handlerInfo = this.getOperationHandlerInfo(request, handlerMapping);
        if (handlerInfo != null) {

            // Set the operation name
            this._data.set_operationName(handlerInfo.getMethod().getName());

            // Also ensure that the correct performance threshold is set for the operation name
            this._data.set_performanceThresholdMilliseconds(
                    this._performanceThresholdCallback.apply(this._data.get_operationName()));
        }
    }

    /*
     * Try to return a match for this request, which will not work for authorizer errors
     * The Spring code may throw an assertion in this case, which we will swallow
     */
    private HandlerMethod getOperationHandlerInfo(
            final HttpServletRequest request,
            final RequestMappingHandlerMapping handlerMapping) {

        try {

            var methods = handlerMapping.getHandlerMethods();
            for (RequestMappingInfo mappingInfo : methods.keySet()) {

                var match = mappingInfo.getMatchingCondition(request);
                if (match != null) {
                    return methods.get(mappingInfo);
                }
            }

            return null;

        } catch (Throwable ex) {
            return null;
        }
    }

    /*
     * Get the data to use when a child operation needs to be managed
     */
    private LogEntryData current() {

        if (this._activeChild != null) {
            return this._activeChild;
        } else {
            return this._data;
        }
    }

    /*
     * Write a single data item by sending the object node to the BareJsonLoggingLayout
     */
    private void writeDataItem(final LogEntryData item) {

        if (this._data.isError()) {
            this._logger.error("error", item.toLogFormat());
        } else {
            this._logger.info("info", item.toLogFormat());
        }
    }
}
