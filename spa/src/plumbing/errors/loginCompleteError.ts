/*
 * This is thrown to short circuit page excecution after processing login responses
 */
export class LoginCompleteError extends Error {

     public constructor() {

        super('Login Complete');

        // Ensure that instanceof works
        Object.setPrototypeOf(this, new.target.prototype);
     }
}
