package com.mycompany.sample.plumbing.spring;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import com.mycompany.sample.plumbing.interceptors.UnhandledExceptionHandler;
import com.mycompany.sample.plumbing.logging.LogEntryImpl;
import com.mycompany.sample.plumbing.oauth.Authorizer;

/*
 * A custom OAuth API filter to allow us to take full control of authorization handling
 * This enables us to plug in a specialist OAuth library while still fitting into an overall Spring API
 */
public final class CustomAuthorizationFilter extends OncePerRequestFilter {

    private final BeanFactory container;

    public CustomAuthorizationFilter(final BeanFactory container) {
        this.container = container;
    }

    /*
     * Do the work of the authorization
     */
    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain) throws ServletException, IOException {

        try {
            // For secured requests, API logging starts here
            var logEntry = this.container.getBean(LogEntryImpl.class);
            logEntry.start(request);

            // Get the authorizer for this HTTP request
            var authorizer = this.container.getBean(Authorizer.class);

            // Do the OAuth work in plain Java classes and return our customised claims
            var claims = authorizer.execute(request);

            // Log who called the API
            logEntry.setIdentity(claims.getToken());

            // Update the Spring security context with the claims
            SecurityContextHolder.getContext().setAuthentication(new CustomAuthentication(claims));

            // Move on to business logic
            filterChain.doFilter(request, response);

        } catch (Throwable ex) {

            // Ensure that authorization errors return the correct exception and CORS details
            var handler = this.container.getBean(UnhandledExceptionHandler.class);
            handler.handleFilterException(request, response, ex);
        }
    }
}
