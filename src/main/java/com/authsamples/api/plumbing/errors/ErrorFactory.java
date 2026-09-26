package com.authsamples.api.plumbing.errors;

import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.StringNode;

/*
 * An error factory class that returns the interface rather than the concrete type
 */
public final class ErrorFactory {

    private ErrorFactory() {
    }

    /*
     * Create an error indicating a server error
     */
    public static ServerError createServerError(String errorCode, String userMessage) {
        return new ServerErrorImpl(errorCode, userMessage);
    }

    /*
     * Create a server error from a caught exception
     */
    public static ServerError createServerError(String errorCode, String userMessage, Throwable cause) {
        return new ServerErrorImpl(errorCode, userMessage, cause);
    }

    /*
     * Create an error indicating a client problem
     */
    public static ClientError createClientError(HttpStatus statusCode, String errorCode, String userMessage) {
        return new ClientErrorImpl(statusCode, errorCode, userMessage);
    }

    /*
     * Create an error indicating a client problem with additional context
     */
    public static ClientError createClientErrorWithContext(
            HttpStatus statusCode,
            String errorCode,
            String userMessage,
            JsonNode logContext) {

        var error = new ClientErrorImpl(statusCode, errorCode, userMessage);
        error.setLogContext(logContext);
        return error;
    }

    /*
     * Create a 401 error with the reason
     */
    public static ClientError createClient401Error(String reason) {

        var error = new ClientErrorImpl(
                HttpStatus.UNAUTHORIZED,
                BaseErrorCodes.INVALID_TOKEN,
                "Missing, invalid or expired access token");

        if (StringUtils.hasLength(reason)) {
            error.setLogContext(new StringNode(reason));
        }

        return error;
    }
}
