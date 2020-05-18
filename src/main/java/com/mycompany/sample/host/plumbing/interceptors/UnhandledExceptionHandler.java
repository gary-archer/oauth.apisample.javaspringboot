package com.mycompany.sample.host.plumbing.interceptors;

import com.mycompany.sample.host.configuration.FrameworkConfiguration;
import com.mycompany.sample.host.plumbing.errors.ApiError;
import com.mycompany.sample.host.plumbing.errors.ApplicationExceptionHandler;
import com.mycompany.sample.host.plumbing.errors.ClientError;
import com.mycompany.sample.host.plumbing.errors.FrameworkErrorUtils;
import com.mycompany.sample.host.plumbing.logging.LogEntryImpl;
import com.mycompany.sample.host.plumbing.utilities.ResponseWriter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * A central point of exception handling
 */
@RestControllerAdvice
public final class UnhandledExceptionHandler {

    private final BeanFactory container;
    private final FrameworkConfiguration configuration;
    private final ApplicationExceptionHandler applicationHandler;

    public UnhandledExceptionHandler(
            final BeanFactory container,
            final FrameworkConfiguration configuration,
            final ApplicationExceptionHandler applicationHandler) {

        this.container = container;
        this.configuration = configuration;
        this.applicationHandler = applicationHandler;
    }

    /*
     * Process API request errors
     */
    @ExceptionHandler(value = Throwable.class)
    public ResponseEntity<String> handleException(final HttpServletRequest request, final Throwable ex) {

        // Allow the application to implement its own error logic first
        var exceptionToHandle = ex;
        exceptionToHandle = this.applicationHandler.translate(exceptionToHandle);

        // Get the log entry for the current request
        var logEntry = this.container.getBean(LogEntryImpl.class);

        // Add error details to logs and get the error to return to the client
        var clientError = this.handleError(exceptionToHandle, logEntry);
        return new ResponseEntity<>(clientError.toResponseFormat().toString(), clientError.getStatusCode());
    }

    /*
     * Process exceptions in filters, which by default do not contain CORS headers
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

        // Output log details, since our logger interceptor does not fire for requests where authentication fail
        logEntry.end(response);
        logEntry.write();

        // Write the response and ensure that the browser client can read it by adding CORS headers
        var writer = new ResponseWriter();
        writer.writeFilterExceptionResponse(request, response, clientError);
    }

    /*
     * An internal routing to log the error details; and return a client error to the caller
     */
    private ClientError handleError(final Throwable ex, final LogEntryImpl logEntry) {

        // Get the error into a known object
        var error = FrameworkErrorUtils.fromException(ex);

        if (error instanceof ApiError) {

            // Handle 5xx errors
            var apiError = (ApiError) error;
            logEntry.setApiError(apiError);
            return apiError.toClientError(this.configuration.getApiName());

        } else {

            // Handle 4xx errors
            ClientError clientError = (ClientError) error;
            logEntry.setClientError(clientError);
            return clientError;
        }
    }
}
