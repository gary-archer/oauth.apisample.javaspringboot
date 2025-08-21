package com.authsamples.api.plumbing.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import com.authsamples.api.plumbing.configuration.LoggingConfiguration;
import com.authsamples.api.plumbing.dependencies.CustomRequestScope;
import com.authsamples.api.plumbing.errors.ClientError;
import com.authsamples.api.plumbing.errors.ErrorUtils;
import com.authsamples.api.plumbing.errors.ServerError;
import com.authsamples.api.plumbing.logging.LogEntryImpl;
import com.authsamples.api.plumbing.logging.LoggerFactory;
import com.authsamples.api.plumbing.utilities.ResponseErrorWriter;

/*
 * A central point of exception handling
 */
@RestControllerAdvice
public final class UnhandledExceptionHandler {

    private final BeanFactory container;
    private final String apiName;
    private final LoggerFactory loggerFactory;

    /*
     * The exception handler requires the name of the API
     */
    public UnhandledExceptionHandler(
            final BeanFactory container,
            final LoggingConfiguration configuration,
            final LoggerFactory loggerFactory) {

        this.container = container;
        this.apiName = configuration.getApiName();
        this.loggerFactory = loggerFactory;
    }

    /*
     * Process API request errors
     */
    @ExceptionHandler(value = Throwable.class)
    public ResponseEntity<String> handleException(final HttpServletRequest request, final Throwable ex) {

        // Get the log entry for the current request
        var logEntry = this.container.getBean(LogEntryImpl.class);

        // Add error details to logs and get the error to return to the client
        var clientError = this.handleError(ex, logEntry);
        var result = new ResponseEntity<>(clientError.toResponseFormat().toString(), clientError.getStatusCode());

        // For this error type, the failure occurs early, and logging interceptors do not fire
        // Therefore output the log entry here, with basic data
        if (ex instanceof UnsatisfiedDependencyException) {

            // Output the request log
            var requestLogger = loggerFactory.getRequestLogger();
            if (requestLogger != null) {
                requestLogger.info("info", logEntry.getRequestLog());
            }
        }

        return result;
    }

    /*
     * Process exceptions in filters
     */
    public void handleFilterException(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Throwable ex) {

        // Get the current log entry
        var logEntry = this.container.getBean(LogEntryImpl.class);

        // Add error details to logs and get the error to return to the client
        var clientError = this.handleError(ex, logEntry);

        // At this point the status is 200 so set it to the correct value
        response.setStatus(clientError.getStatusCode().value());

        // Finish logging of failed requests
        var handlerMappings = this.container.getBean(RequestMappingHandlerMapping.class);
        logEntry.end(request, response, handlerMappings);

        // Output the request log
        var requestLogger = loggerFactory.getRequestLogger();
        if (requestLogger != null) {
            requestLogger.info("info", logEntry.getRequestLog());
        }

        // Clean up per request dependencies
        CustomRequestScope.removeAll();

        // Return error responses to the caller
        var writer = new ResponseErrorWriter();
        writer.writeFilterExceptionResponse(response, clientError);
    }

    /*
     * An internal method to log the error details and return a client error to the caller
     */
    private ClientError handleError(final Throwable ex, final LogEntryImpl logEntry) {

        // Get the error into a known object
        var error = ErrorUtils.fromException(ex);
        if (error instanceof ServerError) {

            // Handle 5xx errors
            var serverError = (ServerError) error;
            logEntry.setServerError(serverError);
            return serverError.toClientError(this.apiName);

        } else {

            // Handle 4xx errors
            ClientError clientError = (ClientError) error;
            logEntry.setClientError(clientError);
            return clientError;
        }
    }
}
