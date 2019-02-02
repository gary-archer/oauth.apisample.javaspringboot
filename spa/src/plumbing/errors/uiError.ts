/*
 * An error class focused on UI scenarios
 */
export class UIError extends Error {

    /*
     * Short technical details to display, depending on the scenario
     */
    private _area: string;
    private _errorCode: string;
    private _utcTime: string;
    private _statusCode: number;
    private _instanceId: number;
    private _details: string;
    private _stack: string;

    /*
     * The common constructor
     */
     public constructor(userMessage: string, area: string, errorCode: string) {

        // A friendly message
        super(userMessage);

        this._area = area;
        this._errorCode = errorCode;
        this._utcTime = '';
        this._statusCode = 0;
        this._instanceId = 0;
        this._details = '';
        this._stack = '';

        // Ensure that instanceof works
        Object.setPrototypeOf(this, new.target.prototype);
     }

    public get area(): string {
        return this._area;
    }

    public get errorCode(): string {
        return this._errorCode;
    }

    public get utcTime(): string {
        return this._utcTime;
    }

    public get statusCode(): number {
        return this._statusCode;
    }

    public set statusCode(value: number) {
        this._statusCode = value;
    }

    public get instanceId(): number {
        return this._instanceId;
    }

    public get details(): string {
        return this._details;
    }

    public get stack(): string {
        return this._stack;
    }

    public set stack(value: string) {
        this._stack = value;
    }

    /*
     * Set short technical details
     */
    public setTechnicalDetails(details: string): void {
        this._details = details;
        this._utcTime = new Date().toISOString();
    }

    /*
     * Override details when an API 500 error is handled
     */
    public setApiErrorDetails(area: string, id: number, utcTime: string) {
        this._area = area;
        this._instanceId = id;
        this._utcTime = utcTime;
    }
}
