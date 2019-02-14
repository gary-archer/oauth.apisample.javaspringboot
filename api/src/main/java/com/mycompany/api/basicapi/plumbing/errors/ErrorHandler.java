package com.mycompany.api.basicapi.plumbing.errors;

import com.mycompany.api.basicapi.configuration.Configuration;
import com.mycompany.api.basicapi.plumbing.utilities.ResponseWriter;
import com.nimbusds.oauth2.sdk.ErrorObject;
import org.javatuples.Pair;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/*
 * A class for handling exceptions, logging them and returning an error to the caller
 */
@RestControllerAdvice
public class ErrorHandler {

    /*
     * The entry point for exceptions on application startup
     */
    public void handleStartupException(Exception ex) {

        // Get the error in a structured form
        var error = (ApiError)this.fromException(ex);

        // Log it before exiting
        var logger = LoggerFactory.getLogger(ErrorHandler.class);
        logger.error(error.toLogFormat().toString());
    }

    /*
     * The entry point for exceptions in controllers
     */
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<String> handleException(HttpServletRequest request, Exception ex) {

        // Get the data
        var responseData = this.handleExceptionInternal(ex);

        // Return an entity based response from the rest controller
        return new ResponseEntity<>(responseData.getValue1(), responseData.getValue0());
    }

    /*
     * The entry point for exceptions in filters
     */
    public void handleFilterException(HttpServletResponse response, Exception ex, String[] trustedOrigins) {

        // Get the data
        var responseData = this.handleExceptionInternal(ex);

        // Write the response
        var writer = new ResponseWriter(trustedOrigins);
        writer.writeFilterExceptionResponse(response, responseData.getValue0(), responseData.getValue1());
    }

    /*
     * Handle an exception and return the response status and serialized body
     */
    private Pair<HttpStatus, String> handleExceptionInternal(Exception ex) {

        // Get the logger
        var logger = LoggerFactory.getLogger(ErrorHandler.class);

        // Get the error into an entity
        var handledError = this.fromException(ex);
        if(handledError instanceof ClientError) {

            // Client errors mean the caller did something wrong
            var error = (ClientError)handledError;

            // Log the error
            logger.error(error.ToLogFormat().toString());

            // Return the typed error to the caller
            return Pair.with(error.getStatusCode(), error.toResponseFormat().toString());
        }
        else {

            // API errors mean we experienced a failure
            var error = (ApiError)handledError;

            // Log the error
            logger.error(error.toLogFormat().toString());

            // Return a client version of the error, which contains an instance id
            var clientError = error.toClientError();
            return Pair.with(clientError.getStatusCode(), clientError.toResponseFormat().toString());
        }
    }

    /*
     * Ensure that the exception has a known type
     */
    private Exception fromException(Exception ex) {

        // Already handled 500 errors
        if (ex instanceof ApiError) {
            return ex;
        }

        // Already handled 4xx errors
        if (ex instanceof ClientError) {
            return ex;
        }

        // Also handle nested exceptions, including those during the async completion phase or application startup errors
        if (ex.getCause() != null) {

            // Get the throwable
            var throwable = ex.getCause();

            // Already handled 500 errors
            if (throwable instanceof ApiError) {
                return (ApiError)throwable;
            }

            // Already handled 4xx errors
            if (throwable instanceof ClientError) {
                return (ClientError)throwable;
            }
        }

        // Create a new API error so that we have structured error data
        var error = new ApiError("general_exception", "An unexpected exception occurred in the API");

        // Set details, including the stack trace of the original exception
        error.setDetails(getExceptionDetails(ex));
        error.setStackTrace(ex.getStackTrace());
        return error;
    }

    /*
     * Return an error during metadata lookup
     */
    public static ApiError fromMetadataError(Exception ex, String url) {

        var apiError = new ApiError("metadata_lookup_failure", "Metadata lookup failed");
        updateErrorDetails(apiError, getExceptionDetails(ex), url);
        return apiError;
    }

    /*
     * Handle introspection errors in the response body
     */
    public static ApiError fromIntrospectionError(ErrorObject errorObject, String url) {

        // Create the error
        var oauthError = getOAuthErrorDetails(errorObject);
        var apiError = createOAuthApiError("introspection_failure", "Token validation failed", oauthError.getValue0());

        // Set technical details
        updateErrorDetails(apiError, oauthError.getValue1(), url);
        return apiError;
    }

    /*
     * Handle exceptions during user info lookup
     */
    public static ApiError fromIntrospectionError(Exception ex, String url) {

        // Already handled from response data
        if(ex instanceof ApiError) {
            return (ApiError)ex;
        }

        var apiError = new ApiError("introspection_failure", "Token validation failed");
        updateErrorDetails(apiError, getExceptionDetails(ex), url);
        return apiError;
    }

    /*
     * Handle user info errors in the response body
     */
    public static ApiError fromUserInfoError(ErrorObject errorObject, String url) {

        // Create the error
        var oauthError = getOAuthErrorDetails(errorObject);
        var apiError = createOAuthApiError("userinfo_failure", "User info lookup failed", oauthError.getValue0());

        // Set technical details
        updateErrorDetails(apiError, oauthError.getValue1(), url);
        return apiError;
    }

    /*
     * Handle exceptions during user info lookup
     */
    public static ApiError fromUserInfoError(Exception ex, String url) {

        // Already handled from response data
        if(ex instanceof ApiError) {
            return (ApiError)ex;
        }

        var apiError = new ApiError("userinfo_failure", "User info lookup failed");
        updateErrorDetails(apiError, getExceptionDetails(ex), url);
        return apiError;
    }

    /*
     * The error thrown if we cannot find an expected claim during OAuth processing
     */
    public static ApiError fromMissingClaim(String claimName) {

        var apiError = new ApiError("claims_failure", "Authorization data not found");
        apiError.setDetails(String.format("An empty value was found for the expected claim %s", claimName));
        return apiError;
    }

    /*
     * Return the error and error_description fields from an OAuth error message
     */
    private static Pair<String, String> getOAuthErrorDetails(ErrorObject errorObject) {

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

    /*
     * Create an error object from an error code and include the OAuth error code in the user message
     */
    private static ApiError createOAuthApiError(String errorCode, String userMessage, String oauthErrorCode) {

        // Include the OAuth error code in the short technical message returned
        String message = userMessage;
        if (!StringUtils.isEmpty(errorCode)) {
            message += String.format(" : %s", errorCode);
        }

        return new ApiError(errorCode, message);
    }

    /*
     * Set details of the error, and include the URL that failed
     */
    private static void updateErrorDetails(ApiError error, String details, String url) {


        var detailsText = "";
        if (!StringUtils.isEmpty(details)) {
            detailsText += details;
        }

        if(!StringUtils.isEmpty(url)) {
            detailsText += String.format(", URL: %s", url);
        }

        error.setDetails(detailsText);

    }

    /*
     * Set a string version of the exception against the API error, which will be logged
     */
    private static String getExceptionDetails(Exception ex) {

        if(ex.getMessage() == null) {
            return String.format("%s : %s", ex.getClass(), Arrays.toString(ex.getStackTrace()));
        } else {
            return String.format("%s : %s", ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
    }
}