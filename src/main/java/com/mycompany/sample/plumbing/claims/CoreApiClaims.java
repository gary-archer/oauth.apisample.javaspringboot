package com.mycompany.sample.plumbing.claims;

import org.springframework.security.core.AuthenticatedPrincipal;
import lombok.Getter;
import lombok.Setter;

/*
 * Claims common to all APIs
 */
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class CoreApiClaims implements AuthenticatedPrincipal {

    // Token claims
    @Getter
    private String subject;

    @Getter
    private String clientId;

    @Getter
    private String[] scopes;

    @Getter
    private int expiry;

    // Name and email claims from the OAuth user info endpoint
    @Getter
    private String givenName;

    @Getter
    private String familyName;

    @Getter
    private String email;

    // The user id from the API's own database
    @Getter
    @Setter
    private String userDatabaseId;

    // Use the access token subject claim as the technical user name
    @Override
    public String getName() {
        return this.subject;
    }

    /*
     * Set token claims after introspection
     */
    public void setTokenInfo(final String subject, final String clientId, final String[] scopes, final int expiry) {
        this.subject = subject;
        this.clientId = clientId;
        this.scopes = scopes;
        this.expiry = expiry;
    }

    /*
     * Set informational fields after user info lookup
     */
    public void setCentralUserInfo(final String givenName, final String familyName, final String email) {
        this.givenName = givenName;
        this.familyName = familyName;
        this.email = email;
    }
}
