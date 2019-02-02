import * as Oidc from 'oidc-client';
import {OAuthConfiguration} from '../../configuration/oauthConfiguration';
import {ErrorHandler} from '../errors/errorHandler';
import {LoginCompleteError} from '../errors/loginCompleteError';

/*
 * The entry point for initiating login and token requests
 */
export class Authenticator {

    /*
     * Used to report silent token renewal errors back to the UI
     */
    private static _errorCallback: (error: any) => void;

    /*
     * The OIDC Client does all of the real security processing
     */
    private _userManager: Oidc.UserManager;

    /*
     * Class setup
     */
    public constructor(config: OAuthConfiguration, errorCallback: (error: any) => void) {

        // Store a reference to the callback
        Authenticator._errorCallback = errorCallback;

        // Create OIDC settings from our application configuration
        const settings = {
            authority: config.authority,
            client_id: config.clientId,
            redirect_uri: config.appUri,
            silent_redirect_uri: config.appUri,
            post_logout_redirect_uri: `${config.appUri}${config.postLogoutPath}`,
            scope: config.scope,
            response_type: 'token id_token',
            loadUserInfo: false,
            automaticSilentRenew: true,
            monitorSession: false,
        };

        // Create the user manager
        this._userManager = new Oidc.UserManager(settings);
        this._userManager.events.addSilentRenewError(this._onSilentTokenRenewalError);
        this._setupCallbacks();
    }

    /*
     * Get an access token and login if required
     */
    public async getAccessToken(): Promise<string> {

        // On most calls we just return the existing token from HTML5 storage
        const user = await this._userManager.getUser();
        if (user && user.access_token && user.access_token.length > 0) {
            return user.access_token;
        }

        // Store the SPA's client side location
        const data = {
            hash: location.hash.length > 0 ? location.hash : '#',
        };

        try {
            // Start a login redirect
            await this._userManager.signinRedirect({state: JSON.stringify(data)});

            // Short circuit SPA page execution
            throw new LoginCompleteError();

        } catch (e) {

            // Handle OAuth specific errors, such as those calling the metadata endpoint
            throw ErrorHandler.getFromOAuthRequest(e, 'login');
        }
    }

    /*
     * Handle the response from the authorization server
     */
    public async handleLoginResponse(): Promise<void> {

        // There is only something to do if we have a query state parameter
        if (location.hash.indexOf('state') === -1) {
            return;
        }

        // See if this is the main window
        if (window.top === window.self) {

            try {
                // Handle the response
                const user = await this._userManager.signinRedirectCallback();
                const data = JSON.parse(user.state);
                location.replace(location.pathname + data.hash);

            } catch (e) {
                // Handle OAuth response errors
                throw ErrorHandler.getFromOAuthResponse(e, 'login');
            }
        } else {
            // Handle silent token renewal responses and note that errors are swallowed by OIDC
            await this._userManager.signinSilentCallback();

            // Short circuit SPA page execution
            throw new LoginCompleteError();
        }
    }

    /*
     * Redirect in order to log out at the authorization server and remove vendor cookies
     */
    public async startLogout(): Promise<void> {

        try {
            await this._userManager.signoutRedirect();
        } catch (e) {
            throw ErrorHandler.getFromOAuthRequest(e, 'logout');
        }
    }

    /*
     * Clear the current access token from storage to force a login
     */
    public async clearAccessToken(): Promise<void> {

        await this._userManager.removeUser();
    }

    /*
     * This method is for testing only, to make the access token in storage act like it has expired
     */
    public async expireAccessToken(): Promise<void> {

        const user = await this._userManager.getUser();
        if (user) {

            // Set the stored value to 60 minutes in the future so that OIDC Client does a token renewal shortly
            // Also corrupt the current token so that there is a 401 if it is sent to the API
            user.expires_at = Date.now() / 1000 + 60;
            user.access_token = 'x' + user.access_token + 'x';

            // Update OIDC so that it silently renews the token almost immediately
            this._userManager.storeUser(user);
            this._userManager.stopSilentRenew();
            this._userManager.startSilentRenew();
        }
    }

    /*
     * Report any silent token renewal errors
     */
    private _onSilentTokenRenewalError(e: any): void {

        // A redirect with 'prompt=none' may return 'login_required', meaning the session expired
        if (e.error !== 'login_required') {

            // Other errors are real problems and are reported
            const error = ErrorHandler.getFromOAuthResponse(e, 'background');
            Authenticator._errorCallback(error);
        }
    }

            /*
     * Plumbing to ensure that the this parameter is available in async callbacks
     */
    private _setupCallbacks(): void {
        this.clearAccessToken = this.clearAccessToken.bind(this);
        this.getAccessToken = this.getAccessToken.bind(this);
        this._onSilentTokenRenewalError = this._onSilentTokenRenewalError.bind(this);
   }
}
