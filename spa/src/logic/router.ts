import * as $ from 'jquery';
import {Authenticator} from '../plumbing/oauth/authenticator';
import {UrlHelper} from '../plumbing/utilities/urlHelper';
import {ErrorView} from './errorView';
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
    private _loadingState: boolean;
    /*
     * Initialize the current view
     */
    public constructor(apiBaseUrl: string, authenticator: Authenticator) {
        this._apiBaseUrl = apiBaseUrl;
        this._authenticator = authenticator;

        // Switch to the loading state on application startup
        this._loadingState = false;
        this._updateControlsDuringLoad();
    }

    /*
     * Execute a view based on the hash URL data
     */
    public async executeView(): Promise<void> {

        // Switch to the loading state while loading a view
        this._updateControlsDuringLoad();

        // Get URL details
        const oldView = this._currentView;
        const hashData = UrlHelper.getLocationHashData();

        // Our simplistic routing works out which main view to show from a couple of known hash fragments
        if (hashData.loggedout) {
            this._currentView = new LogoutView();
        } else {
            if (hashData.company) {
                this._currentView = new TransactionsView(this._authenticator, this._apiBaseUrl, hashData.company);
            } else {
                this._currentView = new ListView(this._authenticator, this._apiBaseUrl);
            }
        }

        // Unload the old view
        if (oldView) {
            oldView.unload();
        }

        // Load the new view
        await this._currentView.execute();

        // Update controls unless logged out
        if (!hashData.loggedout) {
            this._updateControlsAfterLoad();
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

    /*
     * Update controls during busy processing and switch to a loading state
     */
    private _updateControlsDuringLoad(): boolean {

        if (!this._loadingState) {
            $('.initiallydisabled').prop('disabled', true);
            $('.initiallydisabled').addClass('disabled');
            this._loadingState = true;
        }

        ErrorView.clear();
        return false;
    }

    /*
     * Update controls upon completion and remove the loading state
     */
    private _updateControlsAfterLoad(): void {

        if (this._loadingState) {
            $('.initiallydisabled').prop('disabled', false);
            $('.initiallydisabled').removeClass('disabled');
        }

        this._loadingState = false;
    }
}
