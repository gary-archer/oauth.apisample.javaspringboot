package com.authsamples.api.plumbing.utilities;

import java.io.IOException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import com.authsamples.api.plumbing.errors.ClientError;

/*
 * A utility class to manage serializing error responses
 */
public final class ResponseErrorWriter {

    /*
     * Private constructor
     */
    private ResponseErrorWriter() {
    }

    /*
     * This blog's clients read a JSON response, to handle OAuth errors in the same way as other errors
     * Also add the standard www-authenticate header for interoperability
     */
    public static void writeFilterExceptionResponse(
            final HttpServletResponse response,
            final ClientError error,
            final String scope) {

        response.setStatus(response.getStatus());
        response.setHeader("content-type", "application/json");

        if (error.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            response.setHeader("www-authenticate",
                    String.format("Bearer error=\"%s\", error_description=\"%s\"",
                            error.getErrorCode(),
                            error.getMessage()
                    )
            );
        }

        if (error.getStatusCode() == HttpStatus.FORBIDDEN) {
            response.setHeader("www-authenticate",
                    String.format("Bearer error=\"%s\", error_description=\"%s\", scope=\"%s\"",
                            error.getErrorCode(),
                            error.getMessage(),
                            scope
                    )
            );
        }

        try {
            response.getWriter().write(error.toResponseFormat().toString());
        } catch (IOException ex) {
            throw new IllegalStateException("IOException writing response body", ex);
        }
    }
}
