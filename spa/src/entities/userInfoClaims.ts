/*
 * User info claims can be returned to the UI or used if the API triggers sending of emails to users
 */
export class UserInfoClaims {

    /*
     * Fields contained
     */
    public givenName: string;
    public familyName: string;
    public email: string;

    /*
     * Construct from input
     */
    public constructor(givenName: string, familyName: string, email: string) {
        this.givenName = givenName;
        this.familyName = familyName;
        this.email = email;
    }
}
