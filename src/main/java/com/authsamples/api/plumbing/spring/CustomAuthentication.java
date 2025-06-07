package com.authsamples.api.plumbing.spring;

import java.util.ArrayList;
import java.util.Collection;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import com.authsamples.api.plumbing.claims.ClaimsPrincipal;

/*
 * A helper class to enable us to return the security principal in Spring Security terms
 */
public final class CustomAuthentication implements Authentication {

    private final ClaimsPrincipal claims;

    /*
     * Construct from the results of authorizer processing
     */
    public CustomAuthentication(final ClaimsPrincipal claims) {
        this.claims = claims;
    }

    /*
     * Return a default result since we are not using authorities or default role based authorization
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new ArrayList<>();
    }

    /*
     * The example API does not need to return a credential
     */
    @Override
    public Object getCredentials() {
        return null;
    }

    /*
     * The example API does not need to return details
     */
    @Override
    public Object getDetails() {
        return null;
    }

    /*
     * Return the claims as the security principal
     */
    @Override
    public Object getPrincipal() {
        return claims;
    }

    /*
     * By the time we are using this class the request is always authenticated
     */
    @Override
    public boolean isAuthenticated() {
        return true;
    }

    /*
     * This is not relevant for the example API
     */
    @Override
    public void setAuthenticated(final boolean b) throws IllegalArgumentException {
    }

    /*
     * Return the subject claim as the technical user identity
     */
    @Override
    public String getName() {
        return this.claims.getSubject();
    }
}
