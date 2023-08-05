package com.mycompany.sample.plumbing.utilities;

import java.io.IOException;
import jakarta.servlet.http.HttpServletResponse;
import com.mycompany.sample.plumbing.errors.ClientError;

/*
 * A utility class to manage serializing error responses
 */
public final class ResponseWriter {

    /*
     * Handle 500 errors in Spring filters
     */
    public void writeFilterExceptionResponse(final HttpServletResponse response, final ClientError clientError) {

        // Indicate a JSON response
        response.setHeader("Content-Type", "application/json");

        // Add the standards based header if required
        final var unauthorizedStatus = 401;
        if (response.getStatus() == unauthorizedStatus && clientError != null) {

            var wwwAuthenticateHeader = String.format("Bearer realm=\"%s\", error=\"%s\", error_description=\"%s\"",
                    "mycompany.com", clientError.getErrorCode(), clientError.getMessage());
            response.setHeader("WWW-Authenticate", wwwAuthenticateHeader);
        }
        try {

            // Also add a more client friendly JSON response with the same fields
            response.setStatus(response.getStatus());
            response.getWriter().write(clientError.toResponseFormat().toString());

        } catch (IOException ex) {
            throw new IllegalStateException("IOException writing response body", ex);
        }
    }
}
