package com.mycompany.api.basicapi.framework.errors;

import com.mycompany.api.basicapi.framework.utilities.ResponseWriter;
import org.slf4j.Logger;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/*
 * A base error handler class
 */
public class BaseErrorHandler {

    /*
     * This can be called when there is an exception in a filter
     */
    public void handleFilterException(HttpServletResponse response, Exception ex, Logger logger, String[] trustedOrigins) {

        // Get the data
        var clientError = this.handleError(ex, logger);

        // Write the response
        var writer = new ResponseWriter(trustedOrigins);
        writer.writeFilterExceptionResponse(response, clientError.getStatusCode(), clientError.toResponseFormat().toString());
    }

    /*
     * Handle an exception and return the response status and serialized body
     */
    public IClientError handleError(Exception ex, Logger logger) {

        // Already handled API errors
        var apiError = this.tryConvertException(ex, ApiError.class);
        if (apiError != null) {

            // Log the error, which will include technical support details
            logger.error(apiError.toLogFormat().toString());

            // Return a client error to the caller
            return apiError.toClientError();
        }

        // If the API has thrown a 4xx error using an IClientError derived type then it is logged here
        var clientError = this.tryConvertException(ex, ClientError.class);
        if(clientError != null) {

            // Log the error, which will only contain basic details
            logger.error(clientError.toLogFormat().toString());

            // Return the thrown error to the caller
            return clientError;
        }

        // Unhandled exceptions
        apiError = this.fromException(ex);
        logger.error(apiError.toLogFormat().toString());
        return apiError.toClientError();

    }

    /*
     * A default implementation for creating an API error from an unrecognised exception
     */
    protected ApiError fromException(Exception ex) {

        // Create a new API error so that we have structured error data
        var error = new ApiError("general_exception", "An unexpected exception occurred in the API");

        // Set details, including the stack trace of the original exception
        error.setDetails(getExceptionDetails(ex));
        error.setStackTrace(ex.getStackTrace());
        return error;
    }

    /*
     * Ensure that the exception has a known type
     */
    protected <T extends Throwable> T tryConvertException(Exception ex, Class<T> runtimeType) {

        // Already handled 500 errors
        if (ex.getClass() == runtimeType) {
            return (T)ex;
        }

        // Also handle nested exceptions, including those during the async completion phase or application startup errors
        if (ex.getCause() != null) {

            // Get the throwable
            var throwable = ex.getCause();

            // Already handled 500 errors
            if (throwable.getClass() == runtimeType) {
                return (T)throwable;
            }
        }

        return null;
    }

    /*
     * Set a string version of the exception against the API error, which will be logged
     */
    protected String getExceptionDetails(Throwable ex) {

        if(ex.getMessage() == null) {
            return String.format("%s : %s", ex.getClass(), Arrays.toString(ex.getStackTrace()));
        } else {
            return String.format("%s : %s", ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        }
    }
}
