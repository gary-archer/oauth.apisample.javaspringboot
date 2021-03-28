package com.mycompany.sample.plumbing.logging;

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

    private final Logger _logger;
    private final LogEntryData _data;
    private boolean _started;
    private boolean _finished;
    private final Function<String, Integer> _performanceThresholdCallback;

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
        return this._data.get_performance().createChild(name);
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
        this._data.set_errorData(error.toLogFormat(this._data.get_apiName()));
        this._data.set_errorCode(error.getErrorCode());
        this._data.set_errorId(error.getInstanceId());
    }

    /*
     * Add a 4xx error to the log data
     */
    public void setClientError(final ClientError error) {
        this._data.set_errorData(error.toLogFormat());
        this._data.set_errorCode(error.getErrorCode());
    }

    /*
     * Enable free text to be added to production logs, though this should be avoided in most cases
     */
    @Override
    public void addInfo(final JsonNode info) {
        this._data.get_infoData().add(info);
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

            // Finish performance measurements
            this._data.get_performance().close();

            // Calculate fields from the response object
            this._data.set_statusCode(response.getStatus());

            // Do normal finalisation, such as setting denormalised fields
            this._data.finalise();
        }
    }

    /*
     * Output the data
     */
    public void write() {
        this.writeDataItem(this._data);
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
