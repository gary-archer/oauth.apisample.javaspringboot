package com.authsamples.api.plumbing.errors;

import java.io.IOException;
import java.util.ArrayList;
import org.jose4j.jwt.consumer.ErrorCodeValidator;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.InvalidJwtSignatureException;
import org.jose4j.lang.UnresolvableKeyException;
import org.springframework.http.HttpStatus;
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
     * Handle token validation errors, meaning we received an invalid token
     */
    public static RuntimeException fromAccessTokenValidationError(final InvalidJwtException ex, final String url) {

        // First collect details from the exception, but without sensitive JWT details
        var parts = new ArrayList<String>();

        var ioException = ErrorUtils.getIOException(ex);
        if (ioException != null) {

            // Report problems downloading token signing keys
            var error = ErrorFactory.createServerError(
                    ErrorCodes.TOKEN_SIGNING_KEYS_DOWNLOAD_ERROR,
                    "Problem downloading token signing keys", ex);

            parts.add(ErrorUtils.getExceptionDetailsMessage(ioException));
            parts.add(String.format("URL: %s", url));
            var details = String.join(", ", parts);
            error.setDetails(new TextNode(details));
            return error;

        } else {

            var context = new StringBuilder();
            var errors = ex.getErrorDetails();
            for (var error: errors) {

                var errorMessage = getSanitizedErrorMessage(ex, error);
                var message = String.format("%s : %s", error.getErrorCode(), errorMessage);
                context.append(message);
            }

            return ErrorFactory.createClient401Error(context.toString());
        }
    }

    /*
     * The error thrown if we cannot find an expected claim during OAuth processing
     * This is the same underlying problem as a missing scope and typically caused by incorrect configuration
     */
    public static ClientError fromMissingClaim(final String claimName) {

        var message = String.format("Missing claim in input: '%s'", claimName);
        return ErrorFactory.createClientErrorWithContext(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.INSUFFICIENT_SCOPE,
                "The token does not contain sufficient scope for this API",
                new TextNode(message));
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
     * When downloading JWKS keys, an IO exception means we could not get JWKS keys
     * This is classified as a 500 error as opposed to a 401 error, since it is no fault of the client
     */
    private static IOException getIOException(final Throwable ex) {

        Throwable inner = ex;
        while (inner != null) {

            if (inner.getClass() == IOException.class) {
                return (IOException) inner;
            }
            inner = inner.getCause();
        }

        return null;
    }

    /*
     * Some jose4j error messages include JWTs, so avoid including these in error logs
     */
    private static String getSanitizedErrorMessage(final Exception ex, final ErrorCodeValidator.Error error) {

        if (ex.getClass() == InvalidJwtSignatureException.class) {
            return "Invalid JWS Signature";
        }

        if (ex.getCause() != null && ex.getCause().getClass() == UnresolvableKeyException.class) {
            return "Unable to find a suitable verification key";
        }

        return error.getErrorMessage();
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
