package com.authsamples.api.plumbing.spring;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import com.authsamples.api.plumbing.interceptors.UnhandledExceptionHandler;
import com.authsamples.api.plumbing.logging.LogEntryImpl;
import com.authsamples.api.plumbing.oauth.OAuthFilter;
import com.authsamples.api.plumbing.utilities.ClaimsPrincipalHolder;

/*
 * A custom OAuth API filter to allow us to take full control of authorization handling
 * This enables us to plug in a specialist OAuth library while still fitting into an overall Spring API
 */
public final class CustomAuthorizationFilter<T> extends OncePerRequestFilter {

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

            // Get the OAuth filter for this HTTP request
            var oauthFilter = this.container.getBean(OAuthFilter.class);

            // Do the OAuth work in plain Java classes and return our customised claims
            var claimsPrincipal = oauthFilter.execute(request);

            // Log who called the API
            logEntry.setIdentity(claimsPrincipal.getSubject());

            // Update the request scoped injectable object's inner contents
            var holder = this.container.getBean(ClaimsPrincipalHolder.class);
            holder.setClaims(claimsPrincipal);

            // Also update Spring security
            SecurityContextHolder.getContext().setAuthentication(new CustomAuthentication(claimsPrincipal));

            // Move on to business logic
            filterChain.doFilter(request, response);

        } catch (Throwable ex) {

            // Return authentication errors in a form that suits the client
            var handler = this.container.getBean(UnhandledExceptionHandler.class);
            handler.handleFilterException(request, response, ex);
        }
    }
}
