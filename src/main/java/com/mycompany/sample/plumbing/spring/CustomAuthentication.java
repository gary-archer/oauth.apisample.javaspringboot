package com.mycompany.sample.plumbing.spring;

import java.util.ArrayList;
import java.util.Collection;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import com.mycompany.sample.plumbing.claims.ApiClaims;

/*
 * A helper class to enable us to return the security principal in Spring Security terms
 */
public final class CustomAuthentication implements Authentication {

    private final ApiClaims claims;

    /*
     * Construct from the results of authorizer processing
     */
    public CustomAuthentication(final ApiClaims claims) {
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
     * We do not want to return a credential
     */
    @Override
    public Object getCredentials() {
        return null;
    }

    /*
     * We do not want to return details other than the principal above
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
     * This is not relevant for our scenario
     */
    @SuppressWarnings("PMD.UncommentedEmptyMethodBody")
    @Override
    public void setAuthenticated(final boolean b) throws IllegalArgumentException {
    }

    /*
     * Return the subject claim as the technical user name
     */
    @Override
    public String getName() {
        return this.claims.getToken().getSubject();
    }
}
