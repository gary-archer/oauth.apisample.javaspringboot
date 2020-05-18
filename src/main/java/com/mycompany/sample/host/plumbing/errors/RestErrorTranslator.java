package com.mycompany.sample.host.plumbing.errors;

import com.mycompany.sample.logic.errors.BusinessError;
import com.mycompany.sample.logic.errors.ErrorCodes;
import org.springframework.http.HttpStatus;

/*
 * A class to translate from application specific business errors to REST client errors
 */
public final class RestErrorTranslator extends ApplicationExceptionHandler {

    /*
     * The host manages translation from business logic errors to REST 4xx errors
     */
    public Throwable translate(final Throwable ex) {

        // Catch errors that will be returned with a 4xx status
        if (ex instanceof BusinessError) {

            var businessError = (BusinessError) ex;

            // Return a REST specific error
            return ErrorFactory.createClientError(
                    this.getStatusCode(businessError),
                    businessError.getErrorCode(),
                    businessError.getMessage());
        }

        return ex;
    }

    /*
     * Calculate the status code based on the type of business error
     */
    private HttpStatus getStatusCode(final BusinessError error) {

        switch (error.getErrorCode()) {

            // Use 404 for these business errors
            case ErrorCodes.COMPANY_NOT_FOUND:
                return HttpStatus.NOT_FOUND;

            // Return 400 by default
            default:
                return HttpStatus.BAD_REQUEST;
        }
    }
}
