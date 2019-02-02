import * as $ from 'jquery';
import * as Oidc from 'oidc-client';
import {IFrameWindowHelper} from '../plumbing/utilities/iframeWindowHelper';
import {OAuthLogger} from '../plumbing/utilities/oauthLogger';

/*
 * Logic related to rendering OIDC Client trace output
 */
export class TraceView {

    /*
     * Initialize trace controls at application startup
     */
    public static initialize(): void {

        // Initialise the logger and get the level
        const level = OAuthLogger.initialize(TraceView._append);

        // Clear the log initially
        if (!IFrameWindowHelper.isIFrameOperation()) {
            TraceView.clear();
        }

        // Hide or show trace details depending on the level
        const traceContainer = IFrameWindowHelper.getMainWindowElement('.tracecontainer');
        if (level === Oidc.Log.NONE) {
            traceContainer.addClass('hide');
        } else {
            traceContainer.removeClass('hide');
        }

        // Hide the trace button until we have output
        const clearButton = IFrameWindowHelper.getMainWindowElement('#btnTrace');
        clearButton.addClass('hide');
    }

    /*
     * If the URL has changed to a value such as #log=info then we change the OIDC Client ogging level accordingly
     */
    public static updateLevelIfRequired() {
        OAuthLogger.updateLevelIfRequired();
    }

    /*
     * Clear trace output
     */
    public static clear(): void {

        // Remove output
        const traceList = IFrameWindowHelper.getMainWindowElement('#trace');
        traceList.html('');

        // Hide the clear button since there is nothing to clear
        const clearButton = IFrameWindowHelper.getMainWindowElement('#btnClearTrace');
        clearButton.addClass('hide');
    }

    /*
     * Append to log output
     */
    private static _append(prefix: string, args: any): void {

        // Get the output
        const text = Array.prototype.slice.call(args).join(' : ');
        const html = `<b>${prefix}</b> : ${text}`;

        // Make sure any trace info on the hidden iframe is routed to the main window
        const traceList = IFrameWindowHelper.getMainWindowElement('#trace');
        traceList.append($('<li>').html(html));

        // Make sure the trace is visible
        const clearButton = IFrameWindowHelper.getMainWindowElement('#btnClearTrace');
        if (clearButton.hasClass('hide')) {
            clearButton.removeClass('hide');
        }
    }
}
