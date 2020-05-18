package com.mycompany.sample.host.plumbing.errors;

/*
 * The full list of known API error codes
 */
public final class ErrorCodes {

    private ErrorCodes() {
    }

    public static final String UNAUTHORIZED_REQUEST = "unauthorized";

    public static final String SERVER_ERROR = "server_error";

    public static final String CLAIMS_FAILURE = "claims_failure";

    public static final String EXCEPTION_SIMULATION = "exception_simulation";

    public static final String METADATA_LOOKUP_FAILURE = "metadata_lookup_failure";

    public static final String INTROSPECTION_FAILURE = "introspection_failure";

    public static final String USERINFO_FAILURE = "userinfo_failure";

    public static final String INVALID_COMPANY_ID = "invalid_company_id";

    public static final String COMPANY_NOT_FOUND = "company_not_found";

    public static final String FILE_READ_ERROR = "file_read_error";
}
