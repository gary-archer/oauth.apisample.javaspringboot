package com.mycompany.sample.plumbing.spring;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import com.mycompany.sample.plumbing.logging.LogEntryImpl;
import com.mycompany.sample.plumbing.oauth.Authorizer;

/*
 * The Spring Boot entry point class for OAuth request handling
 */
public final class CustomAuthenticationManager implements AuthenticationManager {

    private final BeanFactory container;
    private final HttpServletRequest request;

    public CustomAuthenticationManager(final BeanFactory container, final HttpServletRequest request) {
        this.container = container;
        this.request = request;
    }

    /*
     * Our implementation does its main work in our own authorizer class
     */
    @Override
    public Authentication authenticate(final Authentication bearerTokenAuthentication) throws AuthenticationException {

        try {

            // For secured requests, API logging starts here
            var logEntry = this.container.getBean(LogEntryImpl.class);
            logEntry.start(this.request);

            // Get the authorizer for this HTTP request
            var authorizer = this.container.getBean(Authorizer.class);

            // Do the OAuth work in plain Java classes and return our customised claims
            var claims = authorizer.execute(this.request);

            // Log who called the API
            logEntry.setIdentity(claims.getToken());

            // Return the Spring security context
            return new CustomAuthentication(claims);

        } catch (Throwable ex) {

            // Throw an AuthenticationException derived error
            // This ensures that the CustomAuthenticationEntryPoint class is called
            throw new InsufficientAuthenticationException("Authorizer Failure", ex);
        }
    }
}
