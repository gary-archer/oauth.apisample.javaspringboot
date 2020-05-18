package com.mycompany.sample.host.plumbing.errors;

/*
 * Error codes that could be used by multiple APIs
 */
public final class ErrorCodes {

    private ErrorCodes() {
    }

    public static final String SERVER_ERROR = "server_error";

    public static final String UNAUTHORIZED_REQUEST = "unauthorized";

    public static final String CLAIMS_FAILURE = "claims_failure";

    public static final String METADATA_LOOKUP_FAILURE = "metadata_lookup_failure";

    public static final String INTROSPECTION_FAILURE = "introspection_failure";

    public static final String USERINFO_FAILURE = "userinfo_failure";

    public static final String EXCEPTION_SIMULATION = "exception_simulation";
}
