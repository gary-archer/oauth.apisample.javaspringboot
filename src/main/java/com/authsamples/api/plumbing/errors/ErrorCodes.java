package com.authsamples.api.plumbing.errors;

/*
 * Error codes that could be used by multiple APIs
 */
@SuppressWarnings("PMD.ClassNamingConventions")
public final class ErrorCodes {

    public static final String SERVER_ERROR = "server_error";

    public static final String INVALID_TOKEN = "invalid_token";

    public static final String TOKEN_SIGNING_KEYS_DOWNLOAD_ERROR = "jwks_download_failure";

    public static final String INSUFFICIENT_SCOPE = "insufficient_scope";

    public static final String JSON_PARSE_ERROR = "json_parse_error";

    public static final String EXCEPTION_SIMULATION = "exception_simulation";

    private ErrorCodes() {
    }
}
