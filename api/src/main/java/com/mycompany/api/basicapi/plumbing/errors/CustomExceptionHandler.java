package com.mycompany.api.basicapi.plumbing.errors;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.concurrent.CompletionException;

/*
 * A class for handling exceptions, logging them and returning an error to the caller
 */
@RestControllerAdvice
public class CustomExceptionHandler {

    /*
     * The entry point for handling exceptions
     */
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<String> handleException(HttpServletRequest request, Exception ex) throws JsonProcessingException {

        // Get the logger
        Logger logger = LoggerFactory.getLogger(CustomExceptionHandler.class);

        // Get the error into an entity
        Exception handledError = this.fromException(ex);
        if(handledError instanceof ClientError) {

            // Client errors mean the caller did something wrong
            ClientError error = (ClientError)handledError;

            // Log the error
            logger.error(error.ToLogFormat().toString());

            // Return the typed error to the caller
            return new ResponseEntity<>(error.toResponseFormat().toString(), HttpStatus.resolve(error.getStatusCode()));
        }
        else {

            // API errors mean we experienced a failure
            ApiError error = (ApiError)handledError;

            // Log the error
            logger.error(error.toLogFormat().toString());

            // Return a client version of the error, which contains an instance id
            return new ResponseEntity<>(error.toClientError().toResponseFormat().toString(), HttpStatus.resolve(error.getStatusCode()));
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

        // Also handle async completion exceptions, which contain the original error as a throwable
        if (ex instanceof CompletionException) {

            // Get the throwable
            Throwable throwable = ((CompletionException)ex).getCause();

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
        ApiError error = new ApiError("Exception", "Problem encountered");
        String details = String.format("%s : %s", ex.getMessage(), Arrays.toString(ex.getStackTrace()));
        error.setDetails(details);
        return error;
    }
}