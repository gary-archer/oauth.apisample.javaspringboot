package com.mycompany.sample.plumbing.interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import com.mycompany.sample.plumbing.configuration.LoggingConfiguration;
import com.mycompany.sample.plumbing.dependencies.CustomRequestScope;
import com.mycompany.sample.plumbing.errors.ClientError;
import com.mycompany.sample.plumbing.errors.ErrorUtils;
import com.mycompany.sample.plumbing.errors.ServerError;
import com.mycompany.sample.plumbing.logging.LogEntryImpl;
import com.mycompany.sample.plumbing.utilities.ResponseWriter;

/*
 * A central point of exception handling
 */
@RestControllerAdvice
public final class UnhandledExceptionHandler {

    private final BeanFactory container;
    private final String apiName;

    /*
     * The exception handler requires the name of the API
     */
    public UnhandledExceptionHandler(
            final BeanFactory container,
            final LoggingConfiguration configuration) {

        this.container = container;
        this.apiName = configuration.getApiName();
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
        return new ResponseEntity<>(clientError.toResponseFormat().toString(), clientError.getStatusCode());
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
        logEntry.write();

        // Clean up per request dependencies
        CustomRequestScope.removeAll();

        // Write the response and ensure that the browser client can read it by adding CORS headers
        var writer = new ResponseWriter();
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
