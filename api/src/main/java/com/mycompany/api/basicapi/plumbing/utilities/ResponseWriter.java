package com.mycompany.api.basicapi.plumbing.utilities;

import com.mycompany.api.basicapi.plumbing.errors.ClientError;
import org.springframework.http.HttpStatus;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/*
 * A utility class to manage serializing error responses
 */
public class ResponseWriter {

    /*
     * Injected dependencies
     */
    private final String[] trustedOrigins;

    /*
     * Initialize from configuration
     */
    public ResponseWriter(String[] trustedOrigins) {
        this.trustedOrigins = trustedOrigins;
    }

    /*
     * Handle 401 errors
     */
    public void writeInvalidTokenResponse(HttpServletResponse response) {

        // First add headers
        response.setHeader("WWW-Authenticate", "Bearer");
        this.addErrorResponseHeaders(response);

        // Create the 401 error object
        var error = new ClientError(HttpStatus.UNAUTHORIZED, "unauthorized", "Missing, invalid or expired access token");

        // Send the data
        this.setErrorResponse(response, error.getStatusCode(), error.toResponseFormat().toString());
    }

    /*
     * Handle 500 errors in Spring filters
     */
    public void writeFilterExceptionResponse(HttpServletResponse response, HttpStatus status, String errorJson) {

        this.addErrorResponseHeaders(response);
        this.setErrorResponse(response, status, errorJson);
    }

    /*
     * When there is an error response in a filter, Spring does not honour the headers we've configured
     * Therefore we re-implement them in the below method
     */
    private void addErrorResponseHeaders(HttpServletResponse response) {

        // Indicate a JSON response
        response.setHeader("Content-Type", "application/json");

        // Including CORS headers so that a browser client can read the error response
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        for (String trustedOrigin : this.trustedOrigins) {
            response.setHeader("Access-Control-Allow-Origin", trustedOrigin);
        }
    }

    /*
     * Set response details and handle the exception
     */
    private void setErrorResponse(HttpServletResponse response, HttpStatus status, String responseBody) {

        try {
            response.setStatus(status.value());
            response.getWriter().write(responseBody);

        } catch(IOException ex) {
            throw new RuntimeException("IOException writing response body", ex);
        }
    }
}
