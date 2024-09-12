package com.authsamples.api.plumbing.utilities;

import java.io.IOException;
import jakarta.servlet.http.HttpServletResponse;
import com.authsamples.api.plumbing.errors.ClientError;

/*
 * A utility class to manage serializing error responses
 */
public final class ResponseErrorWriter {

    /*
     * This blog's examples use a JSON response to provide client friendly OAuth errors
     * When required, such as to inform clients how to integrate, a www-authenticate header can be added here
     * - https://datatracker.ietf.org/doc/html/rfc6750#section-3
     */
    public void writeFilterExceptionResponse(final HttpServletResponse response, final ClientError clientError) {

        response.setHeader("content-type", "application/json");

        try {

            response.setStatus(response.getStatus());
            response.getWriter().write(clientError.toResponseFormat().toString());

        } catch (IOException ex) {
            throw new IllegalStateException("IOException writing response body", ex);
        }
    }
}
