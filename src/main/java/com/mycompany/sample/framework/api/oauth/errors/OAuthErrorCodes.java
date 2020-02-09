package com.mycompany.sample.framework.api.oauth.errors;

/*
 * A list of known OAuth related error codes
 */
public final class OAuthErrorCodes {

    private OAuthErrorCodes() {
    }

    public static final String METADATA_LOOKUP_FAILURE = "metadata_lookup_failure";

    public static final String INTROSPECTION_FAILURE = "introspection_failure";

    public static final String USERINFO_FAILURE = "userinfo_failure";
}
