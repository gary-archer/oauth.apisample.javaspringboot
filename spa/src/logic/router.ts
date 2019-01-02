import * as $ from 'jquery';
import {Authenticator} from '../plumbing/authenticator';
import {OAuthLogger} from '../plumbing/oauthLogger';
import {UrlHelper} from '../plumbing/urlHelper';
import {ListView} from './listView';
import {LogoutView} from './logoutView';
import {TransactionsView} from './transactionsView';
import {UserInfoView} from './userInfoView';

/*
 * A very primitive router to deal with switching views
 */
export class Router {

    /*
     * Fields
     */
    private _apiBaseUrl: string;
    private _authenticator: Authenticator;
    private _currentView: any;

    /*
     * Initialize the current view
     */
    public constructor(apiBaseUrl: string, authenticator: Authenticator) {
        this._apiBaseUrl = apiBaseUrl;
        this._authenticator = authenticator;
    }

    /*
     * Execute a view based on the hash URL data
     */
    public async executeView(): Promise<void> {

        // Disable buttons until ready
        $('.initiallydisabled').prop('disabled', true);
        $('.initiallydisabled').addClass('disabled');

        // Get URL details
        const oldView = this._currentView;
        const hashData = UrlHelper.getLocationHashData();

        // Work out which view to show
        if (hashData.loggedout) {
            this._currentView = new LogoutView();
        } else {
            if (hashData.company) {
                this._currentView = new TransactionsView(this._authenticator, this._apiBaseUrl, hashData.company);
            } else {
                this._currentView = new ListView(this._authenticator, this._apiBaseUrl);
            }
        }

        // Update common elements of the frame window when running a new view
        $('#error').text('');

        // Unload the old view
        if (oldView) {
            oldView.unload();
        }

        // Run the new view
        await this._currentView.execute();

        // Enable buttons unless logged out
        if (!hashData.loggedout) {
            $('.initiallydisabled').prop('disabled', false);
            $('.initiallydisabled').removeClass('disabled');
        }
    }

    /*
     * Show the user info child view unless we are logged out
     */
    public async executeUserInfoView(): Promise<void> {

        const hashData = UrlHelper.getLocationHashData();
        if (!hashData.loggedout) {
            const view = new UserInfoView(this._authenticator, this._apiBaseUrl);
            await view.execute();
        }
    }
}
