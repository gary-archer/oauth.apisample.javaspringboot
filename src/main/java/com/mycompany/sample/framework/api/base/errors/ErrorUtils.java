package com.mycompany.sample.framework.api.base.errors;

import com.fasterxml.jackson.databind.node.TextNode;
import com.mycompany.sample.framework.base.ExtendedRuntimeException;
import org.springframework.util.StringUtils;

/*
 * General error utility functions
 */
public final class ErrorUtils {

    private ErrorUtils() {
    }

    /*
     * Return a known error from a general exception
     */
    public static Object fromException(final Throwable exception) {

        var apiError = ErrorUtils.tryConvertToApiError(exception);
        if (apiError != null) {
            return apiError;
        }

        var clientError = ErrorUtils.tryConvertToClientError(exception);
        if (clientError != null) {
            return clientError;
        }

        return ErrorUtils.createApiError(exception, null, null);
    }

    /*
     * Create an error from an exception
     */
    public static ApiError createApiError(final Throwable exception, final String errorCode, final String message) {

        var defaultErrorCode = BaseErrorCodes.SERVER_ERROR;
        var defaultMessage = "An unexpected exception occurred in the API";

        // Create a default error and set a default technical message
        // To customise details instead, application code should use error translation and throw an ApiError
        var error = ErrorFactory.createApiError(
                errorCode == null ? defaultErrorCode : errorCode,
                message == null ? defaultMessage : message,
                exception);

        // Extract details from the original exception
        error.setDetails(new TextNode(getExceptionDetailsMessage(exception)));
        return error;
    }

    /*
     * The error thrown if we cannot find an expected claim during OAuth processing
     */
    public static ApiError fromMissingClaim(final String claimName) {

        var apiError = ErrorFactory.createApiError(BaseErrorCodes.CLAIMS_FAILURE, "Authorization data not found");
        var message = String.format("An empty value was found for the expected claim %s", claimName);
        apiError.setDetails(new TextNode(message));
        return apiError;
    }

    /*
     * Get the error as an API error if applicable
     */
    private static ApiError tryConvertToApiError(final Throwable ex) {

        // Already handled 500 errors
        if (ex instanceof ApiError) {
            return (ApiError) ex;
        }
        if (ex instanceof ExtendedRuntimeException) {
            return ErrorUtils.fromExtendedRuntimeException((ExtendedRuntimeException) ex);
        }

        // Check inner exceptions contained in async exceptions or bean creation exceptions
        var throwable = ex.getCause();
        while (throwable != null) {

            // Already handled 500 errors
            if (throwable instanceof ApiError) {
                return (ApiError) throwable;
            }
            if (throwable instanceof ExtendedRuntimeException) {
                return ErrorUtils.fromExtendedRuntimeException((ExtendedRuntimeException) throwable);
            }

            // Move to next
            throwable = throwable.getCause();
        }

        return null;
    }

    /*
     * Get the error as an IClientError derived error if applicable
     */
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
     * Convert from our custom runtime exception to an API error
     */
    private static ApiError fromExtendedRuntimeException(final ExtendedRuntimeException ex) {

        var apiError = ErrorFactory.createApiError(
                ex.getErrorCode(),
                ex.getMessage(),
                ex);

        var details = ex.getDetails();
        if (!StringUtils.isEmpty(details)) {
            apiError.setDetails(details);
        }

        return apiError;
    }

    /*
     * Set a string version of the exception against the API error, which will be logged
     */
    private static String getExceptionDetailsMessage(final Throwable ex) {

        if (ex.getMessage() == null) {
            return String.format("%s", ex.getClass());
        } else {
            return String.format("%s", ex.getMessage());
        }
    }
}
