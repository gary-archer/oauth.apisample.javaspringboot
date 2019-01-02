import * as $ from 'jquery';
import {UIError} from '../entities/uiError';
import {IFrameWindowHelper} from './iframeWindowHelper';

/*
 * A class to handle composing and reporting errors
 */
export class ErrorHandler {

    /*
     * Output fields from an AppError object, depending on what is populated
     */
    public static reportError(exception: any): void {

        // Ensure that the error is of type UIError
        const error = ErrorHandler.getFromException(exception);

        // Only report real errors
        if (error.nonError) {
            return;
        }

        // Otherwise display the error
        this._outputError(error);
    }

    /*
     * Clear trace output
     */
    public static clear(): void {

        // Remove output
        const errorList = IFrameWindowHelper.getMainWindowElement('#error');
        errorList.html('');

        // Hide the clear button since
        const clearButton = IFrameWindowHelper.getMainWindowElement('#btnClearError');
        clearButton.addClass('hide');
    }

    /*
     * A non error is used to short circuit execution without displaying an error
     */
    public static getNonError(): UIError {

        return new UIError({
            nonError: true,
        });
    }

    /*
     * Sign in request errors most commonly mean a CORS error or that the API is unavailable
     */
    public static getFromOAuthRequest(e: any): UIError {

        // Already handled errors
        if (e instanceof UIError) {
            return e;
        }

        let message = 'Authentication request error';
        if (e.message) {
            message += `: ${e.message}`;
        }

        // Update fields and use a status of zero to hint that it may be a CORS error
        return new UIError({
            message,
            statusCode: 0,
            area: 'OAuth / CORS',
            details: e.toString(),
        });
    }

    /*
     * Sign in response errors most commonly have OAuth error details
     */
    public static getFromOAuthResponse(e: any): UIError {

        // Already handled errors
        if (e instanceof UIError) {
            return e;
        }

        // Handle OAuth errors
        const messagePrefix = 'Authentication response error';
        if (e.error && e.error_description) {

            return new UIError({
                message: `${messagePrefix} : ${e.error}`,
                statusCode: 400,
                area: 'OAuth',
                details: e.error_description,
            });
        }

        let message = messagePrefix;
        if (e.message) {
            message += `: ${e.message}`;
        }

        return new UIError({
            message,
            statusCode: 400,
            area: 'OAuth',
            details: e.toString(),
        });
    }

    /*
     * Return an object for Ajax errors
     */
    public static getFromAjaxError(xhr: any, url: string): UIError {

        // Already handled errors
        if (xhr instanceof UIError) {
            return xhr;
        }

        const error = new UIError({
            message: 'Error calling server',
            statusCode: xhr.status,
            area: 'Ajax',
            url,
        });

        if (xhr.status === 0 ) {

            error.area += ' / CORS';
            error.message = 'Cross origin request was not allowed';

        } else if (xhr.status === 200 ) {

            error.area = ' / JSON';
            error.message = 'Parsing JSON data failed';

        } else {

            // See if there is an API error
            const clientError = ErrorHandler._getApiErrorFromResponse(xhr.responseText);
            if (clientError && clientError.area && clientError.message && clientError.id) {
                error.area = `API / ${clientError.area}`;
                error.message = clientError.message;
                error.instanceId = clientError.id;
            }
        }

        return error;
    }

    /*
     * Return an error based on the exception type or properties
     */
    public static getFromException(e: any): UIError {

        // Already handled errors
        if (e instanceof UIError) {
            return e;
        }

        // Well coded errors should derive from this base class
        if (e instanceof Error) {

            const uiError = new UIError({
                message: `Problem encountered`,
                area: 'Exception',
                details: e.message,
            });
            uiError.stack = e.stack;
            return uiError;
        }

        // For other errors we just call toString
        return new UIError({
            message: 'Problem encountered',
            area: 'Exception',
            details: e.toString(),
        });
    }

    /*
     * Try to deserialize an API error object
     */
    private static _getApiErrorFromResponse(responseText: string): any {

        try {
            return JSON.parse(responseText);
        } catch (e) {
            return null;
        }
    }

    /*
     * Render the error to the UI
     */
    private static _outputError(error: UIError): void {

        const errorContainer = $('#error');
        errorContainer.text('');

        if (error.message.length > 0) {
            errorContainer.append($('<li>').html(`Message : <b>${error.message}</b>`));
        }

        if (error.statusCode > -1) {
            errorContainer.append($('<li>').html(`Status Code : <b>${error.statusCode}</b>`));
        }

        if (error.area.length > 0) {
            errorContainer.append($('<li>').html(`Area : <b>${error.area}</b>`));
        }

        if (error.instanceId) {
            $('#error').append($('<li>').html(`Id : <b>${error.instanceId}</b>`));
        }

        if (error.url.length > 0) {
            errorContainer.append($('<li>').html(`URL : <b>${error.url}</b>`));
        }

        $('#error').append($('<li>').html(`Time : <b>${error.time.toUTCString()}</b>`));

        if (error.details.length > 0) {
            errorContainer.append($('<li>').html(`Details : <b>${error.details}</b>`));
        }

        // Stack info is available for debugging
        // $('#error').append($('<li>').html(`Stack : <b>${error.stack}</b>`));

        // Show the clear button
        const clearButton = $('#btnClearError');
        if (clearButton.hasClass('hide')) {
            clearButton.removeClass('hide');
        }
    }
}
