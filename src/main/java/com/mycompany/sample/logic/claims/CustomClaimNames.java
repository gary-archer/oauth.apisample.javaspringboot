package com.mycompany.sample.logic.claims;

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

    // Custom claims looked up from the API's own data
    public static final String Title = "title";
    public static final String Regions = "regions";
}
