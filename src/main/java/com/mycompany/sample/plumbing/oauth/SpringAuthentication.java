package com.mycompany.sample.plumbing.oauth;

import java.util.Collection;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import com.mycompany.sample.plumbing.claims.CoreApiClaims;
import org.springframework.security.core.authority.AuthorityUtils;

/*
 * A helper class to enable us to return the results of OAuth processing in Spring Security terms
 */
public class SpringAuthentication implements Authentication {

    private final CoreApiClaims claims;

    /*
     * Construct from the results of introspection and claims lookup
     */
    public SpringAuthentication(final CoreApiClaims claims) {
        this.claims = claims;
    }

    /*
     * Spring security requires a granted authorities list based on roles
     * We need to create a default list in order to avoid access denied problems
     * https://www.baeldung.com/spring-security-granted-authority-vs-role
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.createAuthorityList();
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
    @Override
    public void setAuthenticated(boolean b) throws IllegalArgumentException {
    }

    /*
     * Return the subject claim as the technical user name
     */
    @Override
    public String getName() {
        return this.claims.getUserId();
    }
}
