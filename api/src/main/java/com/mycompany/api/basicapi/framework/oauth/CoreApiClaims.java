package com.mycompany.api.basicapi.framework.oauth;

import lombok.Getter;
import org.springframework.security.core.AuthenticatedPrincipal;

/*
 * A base class for API claims
 */
public class CoreApiClaims implements AuthenticatedPrincipal {

    // The immutable user id from the access token, which may exist in the API's database
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
    public void setTokenInfo(String userId, String clientId, String[] scopes) {
        this.userId = userId;
        this.clientId = clientId;
        this.scopes = scopes;
    }

    /*
     * Set informational fields after user info lookup
     */
    public void setCentralUserInfo(String givenName, String familyName, String email) {
        this.givenName = givenName;
        this.familyName = familyName;
        this.email = email;
    }
}
