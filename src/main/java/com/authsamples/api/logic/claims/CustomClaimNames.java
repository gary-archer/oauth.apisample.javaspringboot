package com.authsamples.api.logic.claims;

/*
 * Custom claims used in the API's authorization, or for user identification
 */
@SuppressWarnings(value = "checkstyle:constantnamecheck")
public final class CustomClaimNames {

    private CustomClaimNames() {
    }

    // Custom claims issued to access tokens
    public static final String ManagerId = "manager_id";
    public static final String Role = "role";
}
