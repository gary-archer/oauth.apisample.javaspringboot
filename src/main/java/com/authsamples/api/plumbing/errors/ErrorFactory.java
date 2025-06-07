package com.authsamples.api.plumbing.errors;

import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

/*
 * An error factory class that returns the interface rather than the concrete type
 */
public final class ErrorFactory {

    private ErrorFactory() {
    }

    /*
     * Create an error indicating a server error
     */
    public static ServerError createServerError(final String errorCode, final String userMessage) {
        return new ServerErrorImpl(errorCode, userMessage);
    }

    /*
     * Create a server error from a caught exception
     */
    public static ServerError createServerError(
            final String errorCode,
            final String userMessage,
            final Throwable cause) {

        return new ServerErrorImpl(errorCode, userMessage, cause);
    }

    /*
     * Create an error indicating a client problem
     */
    public static ClientError createClientError(
        final HttpStatus statusCode,
        final String errorCode,
        final String userMessage) {

        return new ClientErrorImpl(statusCode, errorCode, userMessage);
    }

    /*
     * Create an error indicating a client problem with additional context
     */
    public static ClientError createClientErrorWithContext(
            final HttpStatus statusCode,
            final String errorCode,
            final String userMessage,
            final JsonNode logContext) {

        var error = new ClientErrorImpl(statusCode, errorCode, userMessage);
        error.setLogContext(logContext);
        return error;
    }

    /*
     * Create a 401 error with the reason
     */
    public static ClientError createClient401Error(final String reason) {

        var error = new ClientErrorImpl(
                HttpStatus.UNAUTHORIZED,
                BaseErrorCodes.INVALID_TOKEN,
                "Missing, invalid or expired access token");

        if (StringUtils.hasLength(reason)) {
            error.setLogContext(new TextNode(reason));
        }

        return error;
    }
}
