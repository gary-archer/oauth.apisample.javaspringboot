package com.mycompany.api.basicapi.errors;

import com.mycompany.api.basicapi.framework.errors.ApiError;
import com.mycompany.api.basicapi.framework.errors.BaseErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import javax.servlet.http.HttpServletRequest;

/*
 * The API is in control of handling its own errors but also uses the framework
 */
@RestControllerAdvice
public class UnhandledExceptionHandler extends BaseErrorHandler {

    /*
     * The entry point for exceptions on application startup
     */
    public void handleStartupException(Exception ex) {

        var logger = LoggerFactory.getLogger(UnhandledExceptionHandler.class);
        super.handleError(ex, logger);
    }

    /*
     * The entry point for exceptions in controllers
     */
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<String> handleException(HttpServletRequest request, Exception ex) {

        // Get the logger
        var logger = LoggerFactory.getLogger(UnhandledExceptionHandler.class);

        // Log the error and return a client error
        var clientError = super.handleError(ex, logger);

        // Return an entity based response from the rest controller
        return new ResponseEntity<String>(clientError.toResponseFormat().toString(), clientError.getStatusCode());
    }
}
