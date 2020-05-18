package com.mycompany.sample.host.plumbing.errors;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.springframework.http.HttpStatus;

/*
 * An error factory class that returns the interface rather than the concrete type
 */
public final class ErrorFactory {

    private ErrorFactory() {
    }

    /*
     * Create an error indicating a server error
     */
    public static ApiError createServerError(final String errorCode, final String userMessage) {
        return new ApiErrorImpl(errorCode, userMessage);
    }

    /*
     * Create a server error from a caught exception
     */
    public static ApiError createServerError(final String errorCode, final String userMessage, final Throwable cause) {
        return new ApiErrorImpl(errorCode, userMessage, cause);
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
            final ObjectNode logContext) {

        var error = new ClientErrorImpl(statusCode, errorCode, userMessage);
        error.setLogContext(logContext);
        return error;
    }

    /*
     * Create a 401 error with the reason
     */
    public static ClientError create401Error(final String reason) {
        var error = new ClientErrorImpl(
                HttpStatus.UNAUTHORIZED,
                ErrorCodes.UNAUTHORIZED_REQUEST,
                "Missing, invalid or expired access token");
        error.setLogContext(new TextNode(reason));
        return error;
    }
}
