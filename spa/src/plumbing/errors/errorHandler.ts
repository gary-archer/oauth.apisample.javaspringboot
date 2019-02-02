import {LoginCompleteError} from './loginCompleteError';
import {UIError} from './uiError';

/*
 * A class to handle error processing
 */
export class ErrorHandler {

    /*
     * Return an error based on the exception type or properties
     */
    public static getFromException(e: any): UIError | LoginCompleteError  {

        // Already handled errors
        if (e instanceof UIError || e instanceof LoginCompleteError) {
            return e;
        }

        // Create the error
        const userMessage = 'A technical problem was encountered in the Web UI';
        const error = new UIError(userMessage, 'Web UI', 'general_exception');

        // Set exception details including a time
        if (e instanceof Error) {
            error.setTechnicalDetails(e.message);
            if (e.stack) {
                error.stack = e.stack;
            }
        } else {
            error.setTechnicalDetails(e.toString());
        }

        return error;
    }

    /*
     * Sign in request errors most commonly mean a CORS error or that the API is unavailable
     */
    public static getFromOAuthRequest(e: any, requestType: string): UIError | LoginCompleteError {

        // Already handled errors
        if (e instanceof UIError || e instanceof LoginCompleteError) {
            return e;
        }

        // Create the error
        const userMessage = 'A technical problem occurred during login processing';
        const error = new UIError(userMessage, 'Login', 'login_request_error');

        // Set short technical details
        if (e.message) {
            error.setTechnicalDetails(e.toString());
        }
        return error;
    }

    /*
     * Sign in response errors most commonly have OAuth error details
     */
    public static getFromOAuthResponse(e: any, area: string): UIError | LoginCompleteError {

        // Already handled errors
        if (e instanceof UIError || e instanceof LoginCompleteError) {
            return e;
        }

        // The user message indicates either 'login' or 'background' processing
        const userMessage = `A technical problem occurred during ${area} processing`;

        // Create the error
        const error = new UIError(userMessage, 'Login', 'login_response_error');

        // Set short technical details
        if (e.error && e.error_description) {
            error.setTechnicalDetails(`${e.error}, ${e.error_description}`);
        } else {
            error.setTechnicalDetails(e.toString());
        }
        return error;
    }

    /*
     * Return an object for Ajax errors
     * The URL is available but we should not display it to the user for confidentiality reasons
     */
    public static getFromAjaxError(xhr: any, url: string): UIError | LoginCompleteError {

        // Already handled errors
        if (xhr instanceof UIError || xhr instanceof LoginCompleteError) {
            return xhr;
        }

        if (xhr.status === 0 ) {

            // This status is generally a CORS or availability problem
            const userMessage = 'A network problem occurred when the Web UI called the server';
            const error = new UIError(userMessage, 'Network', 'api_uncontactable');
            error.setTechnicalDetails('API not available or request was not allowed');
            return error;

        } else if (xhr.status === 200 ) {

            // This status is generally a JSON parsing error
            const userMessage = 'A technical problem occurred when the Web UI received data';
            const error = new UIError(userMessage, 'Data', 'api_data_error');
            error.setTechnicalDetails('Unable to parse data from API response');
            return error;

        } else {

            // Otherwise we should have a server response in most cases
            const serverError = ErrorHandler._deserializeApiErrorResponse(xhr.responseText);
            return this._getApiError(xhr.status, serverError);
        }
    }

    /*
     * Safely deserialize an error object from the API
     */
    private static _deserializeApiErrorResponse(responseText: string): any {

        try {
            return JSON.parse(responseText);
        } catch (e) {
            return null;
        }
    }

    /*
     * Read API response details
     */
    private static _getApiError(statusCode: number, serverError: any): UIError {

        const userMessage = 'A technical problem occurred when the Web UI called the server';
        if (serverError) {
            if (serverError.code && serverError.message) {

                // Create the error and set the status, for both 4xx and 500 errors
                const error = new UIError(userMessage, 'API', serverError.code);
                error.setTechnicalDetails(serverError.message);
                error.statusCode = statusCode;

                // See if we have extra fields for 500 errors
                if (serverError.area && serverError.id && serverError.utcTime) {
                    error.setApiErrorDetails(serverError.area, serverError.id, serverError.utcTime);
                }

                return error;
            }
        }

        // Return a default error if we cannot get anything from the response
        const defaultError = new UIError(userMessage, 'API', 'general_api_error');
        defaultError.statusCode = statusCode;
        return defaultError;
    }
}
