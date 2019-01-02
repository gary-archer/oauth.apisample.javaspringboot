import * as $ from 'jquery';
import {UserInfoClaims} from '../entities/userInfoClaims';
import {Authenticator} from '../plumbing/authenticator';
import {HttpClient} from '../plumbing/httpClient';

/*
 * Logic related to user info
 */
export class UserInfoView {

    /*
     * Fields
     */
    private _authenticator: Authenticator;
    private _apiBaseUrl: string;

    /*
     * Class setup
     */
    public constructor(authenticator: Authenticator, apiBaseUrl: string) {
        this._authenticator = authenticator;
        this._apiBaseUrl = apiBaseUrl;
        this._setupCallbacks();
    }

    /*
     * Wait for data then render it
     */
    public async execute(): Promise<void> {

        const data = await HttpClient.callApi(
            `${this._apiBaseUrl}/userclaims/current`, 'GET', null, this._authenticator) as UserInfoClaims;

        this._renderData(data);
    }

    /*
     * Render data
     */
    private _renderData(claims: UserInfoClaims): void {

        if (claims.givenName && claims.familyName) {
            $('.logincontainer').removeClass('hide');
            $('.logintext').text(`${claims.givenName} ${claims.familyName}`);
        }
    }

    /*
     * Plumbing to ensure that the this parameter is available in async callbacks
     */
    private _setupCallbacks(): void {
        this._renderData = this._renderData.bind(this);
   }
}
