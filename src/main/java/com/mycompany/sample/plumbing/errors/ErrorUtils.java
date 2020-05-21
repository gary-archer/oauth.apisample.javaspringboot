package com.mycompany.sample.plumbing.errors;

import org.javatuples.Pair;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.node.TextNode;
import com.nimbusds.oauth2.sdk.ErrorObject;

/*
 * A class for managing error translation into a loggable form
 */
public final class ErrorUtils {

    private ErrorUtils() {
    }

    /*
     * Return a known error from a general exception
     */
    public static Object fromException(final Throwable exception) {

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
     * Return an error during metadata lookup
     */
    public static ServerError fromMetadataError(final Throwable ex, final String url) {

        var error = ErrorFactory.createServerError(
                ErrorCodes.METADATA_LOOKUP_FAILURE,
                "Metadata lookup failed", ex);
        ErrorUtils.setErrorDetails(error, null, ex, url);
        return error;
    }

    /*
     * Handle introspection errors in the response body
     */
    public static ServerError fromIntrospectionError(final ErrorObject errorObject, final String url) {

        // Create the error
        var oauthError = ErrorUtils.readOAuthErrorResponse(errorObject);
        var serverError = createOAuthServerError(
                ErrorCodes.INTROSPECTION_FAILURE,
                "Token validation failed",
                oauthError.getValue0());

        // Set technical details
        ErrorUtils.setErrorDetails(serverError, oauthError.getValue1(), null, url);
        return serverError;
    }

    /*
     * Handle exceptions during user info lookup
     */
    public static ServerError fromIntrospectionError(final Throwable ex, final String url) {

        // Already handled from response data
        if (ex instanceof ServerError) {
            return (ServerError) ex;
        }

        // Already handled due to invalid token
        if (ex instanceof ClientError) {
            throw (ClientError) ex;
        }

        var error = ErrorFactory.createServerError(
                ErrorCodes.INTROSPECTION_FAILURE,
                "Token validation failed", ex);
        ErrorUtils.setErrorDetails(error, null, ex, url);
        return error;
    }

    /*
     * Handle user info errors in the response body
     */
    public static ServerError fromUserInfoError(final ErrorObject errorObject, final String url) {

        // Create the error
        var oauthError = ErrorUtils.readOAuthErrorResponse(errorObject);
        var serverError = createOAuthServerError(
                ErrorCodes.USERINFO_FAILURE,
                "User info lookup failed", oauthError.getValue0());

        // Set technical details
        ErrorUtils.setErrorDetails(serverError, oauthError.getValue1(), null, url);
        return serverError;
    }

    /*
     * Handle exceptions during user info lookup
     */
    public static ServerError fromUserInfoError(final Throwable ex, final String url) {

        // Already handled from response data
        if (ex instanceof ServerError) {
            return (ServerError) ex;
        }

        // Already handled due to invalid token
        if (ex instanceof ClientError) {
            throw (ClientError) ex;
        }

        var error = ErrorFactory.createServerError(ErrorCodes.USERINFO_FAILURE, "User info lookup failed", ex);
        ErrorUtils.setErrorDetails(error, null, ex, url);
        return error;
    }

    /*
     * The error thrown if we cannot find an expected claim during OAuth processing
     */
    public static ServerError fromMissingClaim(final String claimName) {

        var serverError = ErrorFactory.createServerError(ErrorCodes.CLAIMS_FAILURE, "Authorization data not found");
        var message = String.format("An empty value was found for the expected claim %s", claimName);
        serverError.setDetails(new TextNode(message));
        return serverError;
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
     * Return the error and error_description fields from an OAuth error message
     */
    private static Pair<String, String> readOAuthErrorResponse(final ErrorObject errorObject) {

        String code = null;
        String description = null;
        if (errorObject != null) {

            if (!StringUtils.isEmpty(errorObject.getCode())) {
                code = errorObject.getCode();
            }

            if (!StringUtils.isEmpty(errorObject.getDescription())) {
                description = errorObject.getDescription();
            }
        }

        return Pair.with(code, description);
    }

    /*i
     * Create an error object from an error code and include the OAuth error code in the user message
     */
    private static ServerError createOAuthServerError(
            final String errorCode,
            final String userMessage,
            final String oauthErrorCode) {

        // Include the OAuth error code in the short technical message returned
        String message = userMessage;
        if (!StringUtils.isEmpty(oauthErrorCode)) {
            message += String.format(" : %s", oauthErrorCode);
        }

        return ErrorFactory.createServerError(errorCode, message);
    }

    /*
     * Update the server error object with technical exception details
     */
    private static void setErrorDetails(
            final ServerError error,
            final String oauthDetails,
            final Throwable ex,
            final String url) {

        var detailsText = "";
        if (!StringUtils.isEmpty(oauthDetails)) {
            detailsText += oauthDetails;
        } else if (ex != null) {
            detailsText += ErrorUtils.getExceptionDetailsMessage(ex);
        }

        if (!StringUtils.isEmpty(url)) {
            detailsText += String.format(", URL: %s", url);
        }

        error.setDetails(new TextNode(detailsText));
    }

    /*
     * Set a string version of the exception details against the server error, which will be logged
     */
    private static String getExceptionDetailsMessage(final Throwable ex) {

        if (ex.getMessage() == null) {
            return String.format("%s", ex.getClass());
        } else {
            return String.format("%s", ex.getMessage());
        }
    }
}
