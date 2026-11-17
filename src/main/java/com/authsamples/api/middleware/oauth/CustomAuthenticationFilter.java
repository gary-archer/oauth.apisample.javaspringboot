package com.authsamples.api.middleware.oauth;

import java.io.IOException;
import java.util.Arrays;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import com.authsamples.api.configuration.OAuthConfiguration;
import com.authsamples.api.middleware.claims.ClaimsReader;
import com.authsamples.api.middleware.errors.ErrorCodes;
import com.authsamples.api.middleware.errors.ErrorFactory;
import com.authsamples.api.middleware.errors.UnhandledExceptionHandler;
import com.authsamples.api.middleware.logging.LogEntryImpl;
import com.authsamples.api.utilities.ClaimsPrincipalHolder;

/*
 * A custom authentication filter to take finer control over processing of tokens and claims
 */
public final class CustomAuthenticationFilter extends OncePerRequestFilter {

    private final BeanFactory container;
    private final OAuthConfiguration configuration;

    public CustomAuthenticationFilter(BeanFactory container) {

        this.container = container;
        this.configuration = this.container.getBean(OAuthConfiguration.class);
    }

    /*
     * Do the main work to process tokens, claims and log identity details
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // For secured requests, API logging starts here
            var logEntry = this.container.getBean(LogEntryImpl.class);
            logEntry.start(request);

            // Get the OAuth filter for this HTTP request
            var oauthFilter = this.container.getBean(OAuthFilter.class);

            // Do the OAuth work in plain Java classes and get customised claims
            var claimsPrincipal = oauthFilter.execute(request);

            // The sample API requires the same scope for all endpoints, and it is enforced here
            var scopes = ClaimsReader.getStringClaim(claimsPrincipal.getJwt(), "scope").split(" ");
            var foundScope = Arrays.stream(scopes).filter(s -> s.contains(this.configuration.getScope())).findFirst();
            if (foundScope.isEmpty()) {
                throw ErrorFactory.createClientError(
                        HttpStatus.FORBIDDEN,
                        ErrorCodes.INSUFFICIENT_SCOPE,
                        "The token does not contain sufficient scope for this API");
            }

            // Update the holder object's data so that we can use constructor injection for the claims principal
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
