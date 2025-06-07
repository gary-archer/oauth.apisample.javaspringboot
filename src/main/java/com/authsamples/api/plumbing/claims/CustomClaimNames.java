package com.authsamples.api.plumbing.claims;

/*
 * Custom claims issued to access tokens
 */
@SuppressWarnings(value = "checkstyle:constantnamecheck")
public final class CustomClaimNames {

    public static final String ManagerId = "manager_id";
    public static final String Role = "role";

    private CustomClaimNames() {
    }
}
