import * as $ from 'jquery';

/*
 * A helper class to ensure that any OAuth trace or error messages are routed to the main window and not the frame
 */
export class IFrameWindowHelper {

    /*
     * Get the main window item
     */
    public static getMainWindowElement(itemName: string): any {
        if (IFrameWindowHelper.isIFrameOperation()) {
            return (window.parent as any).$(itemName);
        } else {
            return $(itemName);
        }
    }

    /*
     * Detect whether a particular operation is running on the silent renew iframe
     */
    public static isIFrameOperation(): boolean {
        return (window.parent && window !== window.parent);
    }
}
