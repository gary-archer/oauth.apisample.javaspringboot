package com.authsamples.api.tests.utils;

import lombok.Getter;
import lombok.Setter;

public class MockTokenOptions {

    @Getter
    @Setter
    private String issuer = "https://login.authsamples-dev.com";

    @Getter
    @Setter
    private String audience = "api.authsamples.com";

    @Getter
    @Setter
    private String scope = "openid profile investments";

    @Getter
    @Setter
    public String delegationId = "";

    @Getter
    @Setter
    public String clientId = "";

    @Getter
    @Setter
    private int expiryMinutes = 15;

    @Getter
    private String subject = "";

    @Getter
    private String managerId = "";

    @Getter
    private String role = "";

    /*
     * Test with the user identities for the standard user
     */
    public void useStandardUser() {
        this.subject = "a6b404b1-98af-41a2-8e7f-e4061dc0bf86";
        this.managerId = "10345";
        this.role = "user";

    }

    /*
     * Test with the user identities for the admin user
     */
    public void useAdminUser() {
        this.subject = "77a97e5b-b748-45e5-bb6f-658e85b2df91";
        this.managerId = "20116";
        this.role = "admin";
    }
}
