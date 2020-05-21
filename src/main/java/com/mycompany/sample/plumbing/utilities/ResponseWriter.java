package com.mycompany.sample.plumbing.utilities;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mycompany.sample.plumbing.errors.ClientError;

/*
 * A utility class to manage serializing error responses
 */
public final class ResponseWriter {

    /*
     * Handle 500 errors in Spring filters
     */
    public void writeFilterExceptionResponse(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final ClientError clientError) {

        this.addErrorResponseHeaders(request, response);
        this.setErrorResponseBody(response, clientError);
    }

    /*
     * When there is an error response in a filter, Spring Boot does not honour the headers we've configured
     * Therefore we re-implement them in the below method, so that browser clients can read the error response
     */
    private void addErrorResponseHeaders(
            final HttpServletRequest request,
            final HttpServletResponse response) {

        // Indicate a JSON response
        response.setHeader("Content-Type", "application/json");

        // Add 401 related errors if required
        final var unauthorizedStatus = 401;
        if (response.getStatus() == unauthorizedStatus) {
            response.setHeader("WWW-Authenticate", "Bearer");
        }

        // Derive the CORS header from the origin header in the request
        String responseHeaderName = "Access-Control-Allow-Origin";
        var responseHeader = response.getHeader(responseHeaderName);
        if (responseHeader == null) {
            var originHeader = request.getHeader("Origin");
            if (originHeader != null) {
                response.setHeader(responseHeaderName, originHeader.toString());
            }
        }
    }

    /*
     * Set response details and handle the exception
     */
    private void setErrorResponseBody(final HttpServletResponse response, final ClientError clientError) {

        try {
            response.setStatus(response.getStatus());
            response.getWriter().write(clientError.toResponseFormat().toString());

        } catch (IOException ex) {
            throw new RuntimeException("IOException writing response body", ex);
        }
    }
}
