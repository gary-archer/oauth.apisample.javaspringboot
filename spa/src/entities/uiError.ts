/*
 * A simple error class for the UI
 */
export class UIError extends Error {

    /*
     * Fields
     */
    private _statusCode: number;
    private _area: string;
    private _url: string;
    private _time: Date;
    private _details: string;
    private _instanceId: number | null;
    private _nonError: boolean;

    /*
     * Let callers supply a subset of named parameters via object destructuring
     */
     public constructor({
        message = '',
        statusCode = -1,
        area = '',
        instanceId = null,
        url = '',
        time = new Date(),
        details = '',
        nonError = false,
    }) {
        super(message);

        // Ensure that instanceof works
        Object.setPrototypeOf(this, new.target.prototype);

        this._statusCode = statusCode;
        this._area = area;
        this._instanceId = instanceId;
        this._url = url;
        this._time = time;
        this._details = details;
        this._nonError = nonError;
    }

    public get statusCode(): number {
        return this._statusCode;
    }

    public set statusCode(statusCode) {
        this._statusCode = statusCode;
    }

    public get area(): string {
        return this._area;
    }

    public set area(area) {
        this._area = area;
    }

    get instanceId() {
        return this._instanceId;
    }

    set instanceId(instanceId) {
        this._instanceId = instanceId;
    }

    public get url(): string {
        return this._url;
    }

    get time() {
        return this._time;
    }

    public get details(): string {
        return this._details;
    }

    public set details(details) {
        this._details = details;
    }

    public get nonError(): boolean {
        return this._nonError;
    }
}
