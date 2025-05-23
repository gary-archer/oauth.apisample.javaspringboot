package com.authsamples.api.plumbing.logging;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import com.authsamples.api.plumbing.errors.ClientError;
import com.authsamples.api.plumbing.errors.ServerError;
import com.fasterxml.jackson.databind.JsonNode;

/*
 * Each API request writes a structured log entry containing fields we will query by
 * This class contains the behaviour and can be injected into business logic if required
 */
public final class LogEntryImpl implements LogEntry {

    private final Logger logger;
    private final LogEntryData data;
    private boolean started;
    private boolean finished;

    /*
     * The default constructor
     */
    public LogEntryImpl(final String apiName, final Logger logger) {
        this(apiName, logger, 1000);
    }

    /*
     * The main constructor
     */
    public LogEntryImpl(
            final String apiName,
            final Logger logger,
            final int performanceThresholdMilliseconds) {

        // Store the logger and initialise state
        this.logger = logger;
        this.started = false;
        this.finished = false;

        // Initialise log data
        this.data = new LogEntryData();
        this.data.setApiName(apiName);
        this.data.setHostName(this.getHostName());
        this.data.setPerformanceThresholdMilliseconds(performanceThresholdMilliseconds);
    }

    /*
     * Start collecting data before calling the API's business logic
     */
    public void start(final HttpServletRequest request) {

        if (!this.started) {
            this.started = true;

            // Start measuring performance
            this.data.getPerformance().start();

            // Calculate fields from the request object
            this.data.setMethod(request.getMethod());
            this.data.setPath(this.getRequestPath(request));

            // Our callers can supply a custom header so that we can keep track of who is calling each API
            var callingApplicationName = request.getHeader("x-authsamples-api-client");
            if (StringUtils.hasLength(callingApplicationName)) {
                this.data.setClientName(callingApplicationName);
            }

            // Use the correlation id from request headers or create one
            var correlationId = request.getHeader("x-authsamples-correlation-id");
            if (StringUtils.hasLength(correlationId)) {
                this.data.setCorrelationId(correlationId);
            } else {
                this.data.setCorrelationId(UUID.randomUUID().toString());
            }

            // Log an optional session id if supplied
            var sessionId = request.getHeader("x-authsamples-session-id");
            if (StringUtils.hasLength(sessionId)) {
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
    public void setIdentity(final String subject) {
        this.data.setUserId(subject);
    }

    /*
     * Create a child performance breakdown when requested
     */
    @Override
    public PerformanceBreakdown createPerformanceBreakdown(final String name) {
        return this.data.getPerformance().createChild(name);
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
        this.data.setErrorData(error.toLogFormat(this.data.getApiName()));
        this.data.setErrorCode(error.getErrorCode());
        this.data.setErrorId(error.getInstanceId());
    }

    /*
     * Add a 4xx error to the log data
     */
    public void setClientError(final ClientError error) {
        this.data.setErrorData(error.toLogFormat());
        this.data.setErrorCode(error.getErrorCode());
    }

    /*
     * Enable free text to be added to production logs, though this should be avoided in most cases
     */
    @Override
    public void addInfo(final JsonNode info) {
        this.data.getInfoData().add(info);
    }

    /*
     * Finish collecting data at the end of the API request and write the output
     */
    public void end(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final RequestMappingHandlerMapping handlerMapping) {

        if (!this.finished) {
            this.finished = true;

            // Set details that are not available at the start of a request
            this.setOperationName(request, handlerMapping);

            // Finish performance measurements
            this.data.getPerformance().close();

            // Calculate fields from the response object
            this.data.setStatusCode(response.getStatus());

            // Do normal finalisation, such as setting denormalised fields
            this.data.finalise();
        }
    }

    /*
     * Output the data
     */
    public void write() {
        this.writeDataItem(this.data);
    }

    /*
     * Try to get the local computer's host name
     */
    private String getHostName() {

        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            return "";
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
            this.data.setOperationName(handlerInfo.getMethod().getName());
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
