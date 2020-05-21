package com.mycompany.sample.plumbing.errors;

import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.databind.node.ObjectNode;

/*
 * An interface for errors returned to the client
 */
public abstract class ClientError extends RuntimeException {

    public ClientError(final String message) {
        super(message);
    }

    // Set additional details returned for API 500 errors
    public abstract void setExceptionDetails(String area, int instanceId, String utcTime);

    // Return the HTTP status code
    public abstract HttpStatus getStatusCode();

    // Return the error code
    public abstract String getErrorCode();

    // Return the JSON response format
    public abstract ObjectNode toResponseFormat();

    // Return the log format
    public abstract ObjectNode toLogFormat();
}
