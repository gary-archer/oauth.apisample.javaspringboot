import * as $ from 'jquery';
import {Configuration} from './configuration/configuration';
import {ErrorView} from './logic/errorView';
import {Router} from './logic/router';
import {TraceView} from './logic/traceView';
import {UIError} from './plumbing/errors/uiError';
import {Authenticator} from './plumbing/oauth/authenticator';
import {HttpClient} from './plumbing/utilities/httpClient';

/*
 * The application class
 */
class App {

    /*
     * The app uses a global instance of OIDC Client and a global router class
     */
    private _authenticator!: Authenticator;
    private _router!: Router;

    /*
     * Class setup
     */
    public constructor() {

        (window as any).$ = $;
        this._setupCallbacks();
    }

    /*
     * The entry point for the SPA
     */
    public async execute(): Promise<void> {

        // Set up click handlers
        $('#btnHome').click(this._onHome);
        $('#btnRefreshData').click(this._onRefreshData);
        $('#btnExpireAccessToken').click(this._onExpireToken);
        $('#btnLogout').click(this._onLogout);
        $('#btnClearError').click(this._onClearError);
        $('#btnClearTrace').click(this._onClearTrace);

        try {
            // Download configuration and then create our main objects
            const config = await this._downloadSpaConfig();
            this._configureAuthentication(config);

            // We must be prepared for page invocation to be an OAuth login response
            await this._handleLoginResponse();

            // Get claims from our API to display the logged in user
            await this._getUserClaims();

            // Execute the main view at the current hash location
            await this._runPage();

        } catch (e) {

            // Render the error view if there are problems
            ErrorView.execute(e);
        }
    }

    /*
     * Download application configuration
     */
    private async _downloadSpaConfig(): Promise<Configuration> {
        return await HttpClient.loadAppConfiguration('spa.config.json');
    }

    /*
     * Initialise authentication related processing
     */
    private _configureAuthentication(config: Configuration): void {

        // Initialise our OIDC Client wrapper
        this._authenticator = new Authenticator(config.oauth, this._onBackgroundError);

        // Set up OIDC Client logging
        TraceView.initialize();

        // Our simple router passes the OIDC Client instance between views
        this._router = new Router(config.app.apiBaseUrl, this._authenticator);
    }

    /*
     * Handle login responses on page load so that we have tokens and can call APIs
     */
    private async _handleLoginResponse(): Promise<void> {
        await this._authenticator.handleLoginResponse();
    }

    /*
     * Get and display user claims from the API, which can contain any data we need, not just token data
     */
    private async _getUserClaims(): Promise<void> {

        try {
            // Get user data and display data in the user region of the page
            await this._router.executeUserInfoView();

        } finally {

            // The above is a one off event and regardless of success we start listening for hash changes
            $(window).on('hashchange', this._onHashChange);
        }
    }

    /*
     * Once login processing has completed, start listening for hash changes
     */
    private async _runPage(): Promise<void> {
        await this._router!.executeView();
    }

    /*
     * Change the view based on the hash URL and catch errors
     */
    private async _onHashChange(): Promise<void> {

        TraceView.updateLevelIfRequired();

        try {
            await this._router.executeView();
        } catch (e) {
            ErrorView.execute(e);
        }
    }

    /*
     * This forces the On Home button to always do a reload of the current view after errors
     */
    private _onHome(): void {

        if (location.hash !== '#home') {
            location.hash = '#home';
        } else {
            location.hash = '#';
        }
    }

    /*
     * Force data reload
     */
    private async _onRefreshData(): Promise<void> {
        try {
            await this._router.executeView();
        } catch (e) {
            ErrorView.execute(e);
        }
    }

    /*
     * Force a new access token to be retrieved
     */
    private async _onExpireToken(): Promise<void> {
        await this._authenticator!.expireAccessToken();
    }

    /*
     * Start a logout request
     */
    private async _onLogout(): Promise<void> {

        try {
           await this._authenticator.startLogout();
        } catch (e) {
            ErrorView.execute(e);
        }
    }

    /*
     * Report background errors during silent token renewal
     */
    private _onBackgroundError(error: UIError): void {
        ErrorView.execute(error);
    }

    /*
     * Clear error output
     */
    private _onClearError(): void {
        ErrorView.clear();
    }

    /*
     * Clear trace output
     */
    private _onClearTrace(): void {
        TraceView.clear();
    }

    /*
     * Plumbing to ensure that the this parameter is available in async callbacks
     */
    private _setupCallbacks(): void {
        this._configureAuthentication = this._configureAuthentication.bind(this);
        this._handleLoginResponse = this._handleLoginResponse.bind(this);
        this._getUserClaims = this._getUserClaims.bind(this);
        this._runPage = this._runPage.bind(this);
        this._onHashChange = this._onHashChange.bind(this);
        this._onHome = this._onHome.bind(this);
        this._onRefreshData = this._onRefreshData.bind(this);
        this._onExpireToken = this._onExpireToken.bind(this);
        this._onLogout = this._onLogout.bind(this);
   }
}

/*
 * Run the application
 */
const app = new App();
app.execute();
