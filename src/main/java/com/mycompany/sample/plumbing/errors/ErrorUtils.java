package com.mycompany.sample.plumbing.errors;

import java.util.ArrayList;
import javax.annotation.Nullable;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

/*
 * A class for managing error translation into a loggable form
 */
public final class ErrorUtils {

    private ErrorUtils() {
    }

    /*
     * Return a known error from a general exception
     */
    public static RuntimeException fromException(final Throwable exception) {

        var serverError = ErrorUtils.tryConvertToServerError(exception);
        if (serverError != null) {
            return serverError;
        }

        var clientError = ErrorUtils.tryConvertToClientError(exception);
        if (clientError != null) {
            return clientError;
        }

        return ErrorUtils.createServerError(exception, null, null);
    }

    /*
     * Create an error from an exception
     */
    public static ServerError createServerError(
            final Throwable exception,
            final String errorCode,
            final String message) {

        var defaultErrorCode = ErrorCodes.SERVER_ERROR;
        var defaultMessage = "An unexpected exception occurred in the API";

        // Create a default error and set a default technical message
        var error = ErrorFactory.createServerError(
                errorCode == null ? defaultErrorCode : errorCode,
                message == null ? defaultMessage : message,
                exception);

        // Extract details from the original exception
        error.setDetails(new TextNode(getExceptionDetailsMessage(exception)));
        return error;
    }

    /*
     * Handle token decode errors, meaning we received invalid input
     */
    public static ClientError fromAccessTokenDecodeError(final Throwable ex) {

        var details = ErrorUtils.getExceptionDetailsMessage(ex);
        var message = String.format("Failed to decode received JWT: %s", details);
        return ErrorFactory.createClient401Error(message);
    }

    /*
     * Handle token signing key download errors, meaning an API technical error
     */
    public static RuntimeException fromTokenSigningKeysDownloadError(final Throwable ex, final String url) {

        // Already handled errors
        if (ex instanceof ClientError) {
            return (ClientError) ex;
        }

        // Create a new error indicating a technical problem
        var error = ErrorFactory.createServerError(
                ErrorCodes.TOKEN_SIGNING_KEYS_DOWNLOAD_ERROR,
                "Problem downloading token signing keys", ex);
        var details = String.format("URL: %s", url);
        error.setDetails(new TextNode(details));
        return error;
    }

    /*
     * Handle token validation errors, meaning we received an invalid token
     */
    public static ClientError fromAccessTokenValidationError(final Throwable ex) {

        var details = ErrorUtils.getExceptionDetailsMessage(ex);
        var message = String.format("Failed to verify JWT: %s", details);
        return ErrorFactory.createClient401Error(message);
    }

    /*
     * Handle exceptions during user info lookup when we may have error details
     */
    public static RuntimeException fromUserInfoError(
            final HttpStatus status,
            final @Nullable ObjectNode responseData,
            final String url) {

        // Collect error parts to get details
        var parts = new ArrayList<String>();
        parts.add("User info lookup failed");
        parts.add(String.format("Status: %s", status.value()));
        if (responseData != null) {
            var errorCodeNode = responseData.get("error");
            if (errorCodeNode != null) {
                parts.add(String.format("Code: %s", errorCodeNode.asText()));
            }
            var errorDescriptionNode = responseData.get("error_description");
            if (errorDescriptionNode != null) {
                parts.add(String.format("Code: %s", errorDescriptionNode.asText()));
            }
        }
        parts.add(String.format("URL: %s", url));
        var details = String.join(", ", parts);

        // Report 401 errors where the access token is rejected
        if (status == HttpStatus.UNAUTHORIZED) {
            return ErrorFactory.createClient401Error(details);
        }

        var error = ErrorFactory.createServerError(ErrorCodes.USERINFO_FAILURE, "User info lookup failed");
        error.setDetails(new TextNode(details));
        return error;
    }

    /*
     * Handle connectivity exceptions during user info lookup
     */
    public static ServerError fromUserInfoError(final Throwable ex, final String url) {

        // Handle rethrown errors
        if (ex instanceof ServerError) {
            return (ServerError) ex;
        }
        if (ex instanceof ClientError) {
            throw (ClientError) ex;
        }

        return ErrorFactory.createServerError(ErrorCodes.USERINFO_FAILURE, "User info lookup failed", ex);
    }

    /*
     * The error thrown if we cannot find an expected claim during OAuth processing
     */
    public static ServerError fromMissingClaim(final String claimName) {

        var error = ErrorFactory.createServerError(ErrorCodes.CLAIMS_FAILURE, "Authorization data not found");
        var message = String.format("An empty value was found for the expected claim '%s'", claimName);
        error.setDetails(new TextNode(message));
        return error;
    }

    /*
     * Convert to a server error if possible
     */
    private static ServerError tryConvertToServerError(final Throwable ex) {

        // Already handled 500 errors
        if (ex instanceof ServerError) {
            return (ServerError) ex;
        }

        // Check inner exceptions contained in async exceptions or bean creation exceptions
        var throwable = ex.getCause();
        while (throwable != null) {

            // Already handled 500 errors
            if (throwable instanceof ServerError) {
                return (ServerError) throwable;
            }

            // Move to next
            throwable = throwable.getCause();
        }

        return null;
    }

    /*
     * Get the error as an IClientError derived error if applicable
     */
    @SuppressWarnings("PMD.CollapsibleIfStatements")
    private static ClientError tryConvertToClientError(final Throwable ex) {

        // Already handled 500 errors
        if (ex instanceof ClientError) {
            return (ClientError) ex;
        }

        // Handle nested exceptions, including those during the async completion phase or application startup errors
        var throwable = ex.getCause();
        if (throwable != null) {

            // Already handled 500 errors
            if (throwable instanceof ClientError) {
                return (ClientError) throwable;
            }
        }

        return null;
    }

    /*
     * Set a string version of the exception details against the server error, which will be logged
     */
    private static String getExceptionDetailsMessage(final Throwable ex) {

        if (ex == null) {
            return "";
        }

        if (ex.getMessage() == null) {
            return String.format("%s", ex.getClass());
        } else {
            return String.format("%s", ex.getMessage());
        }
    }
}
