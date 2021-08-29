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
    public static RuntimeException fromIntrospectionError(final Throwable ex, final String url) {

        // Already handled from response data
        if (ex instanceof ServerError) {
            return (ServerError) ex;
        }

        // Already handled due to invalid token
        if (ex instanceof ClientError) {
            return (ClientError) ex;
        }

        var error = ErrorFactory.createServerError(
                ErrorCodes.INTROSPECTION_FAILURE,
                "Token validation failed", ex);
        ErrorUtils.setErrorDetails(error, null, ex, url);
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
    public static RuntimeException fromTokenSigningKeysDownloadError(final Throwable ex, final String jwksUri) {

        // Already handled errors
        if (ex instanceof ClientError) {
            return (ClientError) ex;
        }

        // Create a new error indicating a technical problem
        var error = ErrorFactory.createServerError(
                ErrorCodes.TOKEN_SIGNING_KEYS_DOWNLOAD_ERROR,
                "Problem downloading token signing keys", ex);
        ErrorUtils.setErrorDetails(error, null, ex, jwksUri);
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
     * Handle user info errors in the response body
     */
    public static RuntimeException fromUserInfoError(final ErrorObject errorObject, final String url) {

        // Read the OAuth error code
        var oauthError = ErrorUtils.readOAuthErrorResponse(errorObject);

        // Handle a race condition where the access token expires during user info lookup
        if (oauthError.getValue0().equals(ErrorCodes.USERINFO_TOKEN_EXPIRED)) {
            return ErrorFactory.createClient401Error("Access token expired during user info lookup");
        }

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
        var message = String.format("An empty value was found for the expected claim '%s'", claimName);
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
     * Return the error and error_description fields from an OAuth error message
     */
    private static Pair<String, String> readOAuthErrorResponse(final ErrorObject errorObject) {

        String code = null;
        String description = null;
        if (errorObject != null) {

            if (StringUtils.hasLength(errorObject.getCode())) {
                code = errorObject.getCode().toLowerCase();
            }

            if (StringUtils.hasLength(errorObject.getDescription())) {
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
        if (StringUtils.hasLength(oauthErrorCode)) {
            message += String.format(" : %s", oauthErrorCode);
        }

        return ErrorFactory.createServerError(errorCode, message);
    }

    /*
     * Update the server error object with technical exception details
     */
    private static void setErrorDetails(
            final ServerError error,
            final String details,
            final Throwable ex,
            final String url) {

        var detailsText = "";
        if (StringUtils.hasLength(details)) {
            detailsText += details;
        } else if (ex != null) {
            detailsText += ErrorUtils.getExceptionDetailsMessage(ex);
        }

        if (StringUtils.hasLength(url)) {
            detailsText += String.format(", URL: %s", url);
        }

        error.setDetails(new TextNode(detailsText));
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
