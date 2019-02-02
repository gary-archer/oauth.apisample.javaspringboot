import * as $ from 'jquery';

/*
 * Logic related to the simple logout view
 */
export class LogoutView {

    /*
     * Run the view
     */
    public async execute(): Promise<void> {

        // Show logout details
        $('#loggedOut').removeClass('hide');
    }

    /*
     * Hide UI elements when the view unloads
     */
    public unload(): void {
        $('#loggedOut').addClass('hide');
    }
}
