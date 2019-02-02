import * as $ from 'jquery';
import * as moment from 'moment';
import {ErrorHandler} from '../plumbing/errors/errorHandler';
import {LoginCompleteError} from '../plumbing/errors/loginCompleteError';
import {UIError} from '../plumbing/errors/uiError';
import {IFrameWindowHelper} from '../plumbing/utilities/iframeWindowHelper';

/*
 * Logic related to rendering errors
 */
export class ErrorView {

    /*
     * Clear existing errors
     */
    public static clear(): void {

        // Clear any content
        const errorList = IFrameWindowHelper.getMainWindowElement('.errorForm');
        errorList.html('');

        // Hide the clear button
        const clearButton = IFrameWindowHelper.getMainWindowElement('#btnClearError');
        if (!clearButton.hasClass('hide')) {
            clearButton.addClass('hide');
        }
    }

    /*
     * Do the error rendering given an exception
     */
    public static execute(exception: any): void {

        // This error is used to short circuit page execution during login processing
        if (exception instanceof LoginCompleteError) {
            return;
        }

        // Ensure that the error is of type UIError and then render it
        const error = ErrorHandler.getFromException(exception) as UIError;
        ErrorView._renderData(error);
    }

    /*
     * Render the error to the UI
     */
    private static _renderData(error: UIError): void {

        // Clear any content
        const errorForm = IFrameWindowHelper.getMainWindowElement('.errorForm');
        $('.errorForm').html('');

        // Show the clear button
        const clearButton = $('#btnClearError');
        if (clearButton.hasClass('hide')) {
            clearButton.removeClass('hide');
        }

        // Show the friendly user message
        if (error.message.length > 0) {
            errorForm.append(ErrorView._getErrorUserMessageRow(error.message));
        }

        // Show technical support details
        if (error.area.length > 0) {
            errorForm.append(ErrorView._getErrorSupportRow('Area', error.area));
        }

        if (error.errorCode.length > 0) {
            errorForm.append(ErrorView._getErrorSupportRow('Error Code', error.errorCode));
        }

        if (error.statusCode > 0) {
            errorForm.append(ErrorView._getErrorSupportRow('Status Code', error.statusCode));
        }

        if (error.instanceId > 0) {
            errorForm.append(ErrorView._getErrorSupportRow('Id', error.instanceId));
        }

        if (error.utcTime.length > 0) {
            const displayTime = moment(error.utcTime).format('DD MMM YYYY HH:mm:ss');
            errorForm.append(ErrorView._getErrorSupportRow('UTC Time', displayTime));
        }

        if (error.details.length > 0) {
            errorForm.append(ErrorView._getErrorSupportRow('Details', error.details));
        }

        // Stack info is available but we don't render it to the user
        // if (error.stack.length > 0) {
        //     errorForm.append(ErrorView._getErrorSupportRow('Details', error.stack));
        // }
    }

    /*
     * Return the user message, which has a larger dark blue font
     */
    private static _getErrorUserMessageRow(userMessage: string): string {

        return `<div class='panel panel-default'>
                    <div class='panel-body'>
                        <div class='row errorUserInfo'>
                            <div class='col-xs-12'>
                                ${userMessage}
                            </div>
                        </div>
                    </div>
                </div>`;
    }

    /*
     * Return a field displayed to help with technical support, which uses a smaller light blue font
     */
    private static _getErrorSupportRow(title: string, value: any): string {

        return `<div class='panel panel-default'>
                    <div class='panel-body'>
                        <div class='row errorSupportInfo'>
                            <div class='col-xs-2'>
                                ${title}
                            </div>
                            <div class='col-xs-10'>
                                <b>${value}</b>
                            </div>
                        </div>
                    </div>
                </div>`;
    }
}
