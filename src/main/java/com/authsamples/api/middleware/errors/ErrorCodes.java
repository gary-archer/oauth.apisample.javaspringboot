package com.authsamples.api.middleware.errors;

/*
 * Error codes to represent error causes
 */
public final class ErrorCodes {

    public static final String SERVER_ERROR = "server_error";

    public static final String INVALID_TOKEN = "invalid_token";

    public static final String TOKEN_SIGNING_KEYS_DOWNLOAD_ERROR = "jwks_download_failure";

    public static final String INSUFFICIENT_SCOPE = "insufficient_scope";

    public static final String JSON_SERIALIZE_ERROR = "json_serialize_error";

    public static final String JSON_PARSE_ERROR = "json_parse_error";

    public static final String EXCEPTION_SIMULATION = "exception_simulation";

    public static final String INVALID_COMPANY_ID = "invalid_company_id";

    public static final String COMPANY_NOT_FOUND = "company_not_found";

    public static final String FILE_READ_ERROR = "file_read_error";

    private ErrorCodes() {
    }
}
