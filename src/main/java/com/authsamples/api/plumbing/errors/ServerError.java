package com.authsamples.api.plumbing.errors;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/*
 * A class to process full error details
 */
public abstract class ServerError extends RuntimeException {

    public ServerError(final String message, final Throwable cause) {
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

    // Return the client error for this service error
    public abstract ClientError toClientError(String apiName);
}
