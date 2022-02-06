package com.mycompany.sample.plumbing.utilities;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import com.mycompany.sample.plumbing.errors.ClientError;

/*
 * A utility class to manage serializing error responses
 */
public final class ResponseWriter {

    /*
     * Handle 500 errors in Spring filters
     */
    public void writeFilterExceptionResponse(final HttpServletResponse response, final ClientError clientError) {

        this.addErrorResponseHeaders(response);
        this.setErrorResponseBody(response, clientError);
    }

    /*
     * Write error headers to the response
     */
    private void addErrorResponseHeaders(final HttpServletResponse response) {

        // Indicate a JSON response
        response.setHeader("Content-Type", "application/json");

        // Add 401 related errors if required
        final var unauthorizedStatus = 401;
        if (response.getStatus() == unauthorizedStatus) {
            response.setHeader("WWW-Authenticate", "Bearer");
        }
    }

    /*
     * Write response body details
     */
    private void setErrorResponseBody(final HttpServletResponse response, final ClientError clientError) {

        try {
            response.setStatus(response.getStatus());
            response.getWriter().write(clientError.toResponseFormat().toString());

        } catch (IOException ex) {
            throw new IllegalStateException("IOException writing response body", ex);
        }
    }
}
