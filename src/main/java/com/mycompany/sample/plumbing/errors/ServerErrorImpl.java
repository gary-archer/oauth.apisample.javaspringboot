package com.mycompany.sample.plumbing.errors;

import java.time.Instant;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/*
 * The default implementation of a server error
 */
public final class ServerErrorImpl extends ServerError {

    // A range for generated instance ids
    private static final int MIN_ERROR_ID = 10000;
    private static final int MAX_ERROR_ID = 99999;

    private final HttpStatus statusCode;
    private final String utcTime;
    private final String errorCode;
    private final int instanceId;
    private JsonNode details;

    /*
     * Construct from an error code and user message
     */
    public ServerErrorImpl(final String errorCode, final String userMessage) {
        this(errorCode, userMessage, null);
    }

    /*
     * Construct from an error code and user message
     */
    public ServerErrorImpl(final String errorCode, final String userMessage, final Throwable cause) {
        super(userMessage, cause);

        this.statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
        this.errorCode = errorCode;
        this.instanceId = (int) Math.floor(Math.random() * (MAX_ERROR_ID - MIN_ERROR_ID + 1) + MIN_ERROR_ID);
        this.utcTime = Instant.now().toString();
    }

    @Override
    public String getErrorCode() {
        return this.errorCode;
    }

    @Override
    public int getInstanceId() {
        return this.instanceId;
    }

    @Override
    public void setDetails(final JsonNode details) {
        this.details = details;
    }

    /*
     * Return a dynamic object that can be serialized by calling toString
     */
    public ObjectNode toLogFormat(final String apiName) {

        var mapper = new ObjectMapper();
        var error = mapper.createObjectNode();

        // Add what we returned to the caller
        error.put("statusCode", this.statusCode.value());
        error.set("clientError", this.toClientError(apiName).toResponseFormat());

        // Add service details
        var serviceError = mapper.createObjectNode();
        serviceError.put("errorCode", this.errorCode);

        // Details can be supplied as either an object node or a string
        if (this.details != null) {
            serviceError.set("details", this.details);
        }

        // Output the stack trace of the original error
        var frames = this.getOriginalCause().getStackTrace();
        if (frames.length > 0) {
            var stackNode = mapper.createArrayNode();
            for (var frame : frames) {
                stackNode.add(frame.toString());
            }

            serviceError.set("stack", stackNode);
        }

        // Finalise and return the result to be included in request logs
        error.set("serviceError", serviceError);
        return error;
    }

    /*
     * Translate to a confidential error that is returned to the API caller, with a reference to the logged details
     */
    public ClientError toClientError(final String apiName) {

        // Set a generic client error code for the server exception
        var error = ErrorFactory.createClientError(this.statusCode, this.errorCode, this.getMessage());

        // Also indicate which part of the system, where in logs and when the error occurred
        error.setExceptionDetails(apiName, this.instanceId, this.utcTime);
        return error;
    }

    /*
     * Get an exception's original cause for call stack reporting
     */
    private Throwable getOriginalCause() {

        Throwable cause = this;
        Throwable inner = this;
        while (inner != null) {
            inner = inner.getCause();
            if (inner != null) {
                cause = inner;
            }
        }

        return cause;
    }
}
