package com.mycompany.sample.framework.api.base.errors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/*
 * An interface for errors internal to the API
 */
public abstract class ApiError extends RuntimeException {

    public ApiError(final String message, final Throwable cause) {
        super(message, cause);
    }

    // Return the error code
    public abstract String getErrorCode();

    // Return an instance id used for error lookup
    public abstract int getInstanceId();

    // Set details from an object node
    public abstract void setDetails(JsonNode details);

    // Return the log format
    public abstract ObjectNode toLogFormat(String apiName);

    // Return the client error for the API error
    public abstract ClientError toClientError(String apiName);
}
