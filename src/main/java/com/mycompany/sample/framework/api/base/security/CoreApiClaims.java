package com.mycompany.sample.framework.api.base.security;

import lombok.Getter;
import org.springframework.security.core.AuthenticatedPrincipal;

/*
 * A base class for claims from an OAuth token
 */
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class CoreApiClaims implements AuthenticatedPrincipal {

    // The immutable user id from the access token
    @Getter
    private String userId;

    // The client id, which typically represents the calling application
    @Getter
    private String clientId;

    // OAuth scopes can represent high level areas of the business
    @Getter
    private String[] scopes;

    // User info fields
    @Getter
    private String givenName;

    @Getter
    private String familyName;

    @Getter
    private String email;

    // We return the immutable user id from the access token as the user name, which is not a display value
    @Override
    public String getName() {
        return userId;
    }

    /*
     * Set token claims after introspection
     */
    public void setTokenInfo(final String userId, final String clientId, final String[] scopes) {
        this.userId = userId;
        this.clientId = clientId;
        this.scopes = scopes;
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
