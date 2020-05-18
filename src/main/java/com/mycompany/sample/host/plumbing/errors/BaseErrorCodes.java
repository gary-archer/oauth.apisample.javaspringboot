package com.mycompany.sample.host.plumbing.errors;

/*
 * A list of base framework error codes
 */
public final class BaseErrorCodes {

    private BaseErrorCodes() {
    }

    public static final String UNAUTHORIZED_REQUEST = "unauthorized";

    public static final String SERVER_ERROR = "server_error";

    public static final String CLAIMS_FAILURE = "claims_failure";

    public static final String EXCEPTION_SIMULATION = "exception_simulation";
}
