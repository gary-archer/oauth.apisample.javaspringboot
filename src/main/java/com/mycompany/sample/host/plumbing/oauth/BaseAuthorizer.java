package com.mycompany.sample.host.plumbing.oauth;

import com.mycompany.sample.host.claims.CoreApiClaims;
import com.mycompany.sample.host.plumbing.utilities.RequestClassifier;
import com.mycompany.sample.host.plumbing.interceptors.UnhandledExceptionHandler;
import com.mycompany.sample.host.plumbing.logging.LogEntryImpl;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

/*
 * A base authorizer that performs common plumbing
 */
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public abstract class BaseAuthorizer extends OncePerRequestFilter {

    /*
     * Injected dependencies
     */
    @Getter(AccessLevel.PROTECTED)
    private final BeanFactory container;

    /*
     * Receive our JSON configuration
     */
    public BaseAuthorizer(final BeanFactory container) {
        this.container = container;
    }

    /*
     * Authorization processing common to all types
     */
    @Override
    protected void doFilterInternal(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final FilterChain filterChain) {

        try {

            // Make sure we only process real API requests
            var requestClassifier = this.container.getBean(RequestClassifier.class);
            if (!requestClassifier.isApiRequest(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            // Get metadata for this request
            var handlerMappings = this.container.getBean(RequestMappingHandlerMapping.class);

            // For secured requests we start logging here
            var logEntry = this.container.getBean(LogEntryImpl.class);
            logEntry.start(request, handlerMappings);

            // Do the authorization work
            var claims = this.execute(request);

            // Log who called the API
            logEntry.setIdentity(claims);

            // Update the Spring security context with the claims
            SecurityContextHolder.getContext().setAuthentication(this.createOAuth2Authentication(claims));

            // Move on to business logic
            filterChain.doFilter(request, response);

        } catch (Throwable ex) {

            // Ensure that authorization errors return the correct exception and CORS details
            var handler = this.container.getBean(UnhandledExceptionHandler.class);
            handler.handleFilterException(request, response, ex);
        }
    }

    // Concrete classes must override this
    protected abstract CoreApiClaims execute(HttpServletRequest request);

    /*
     * Plumbing to update Spring Boot's security objects
     */
    private OAuth2Authentication createOAuth2Authentication(final CoreApiClaims claims) {

        // Spring security requires a granted authorities list based on roles
        // We need to create a default list in order to avoid access denied problems
        // https://www.baeldung.com/spring-security-granted-authority-vs-role
        var authorities = AuthorityUtils.createAuthorityList();

        // Create the OAuth request object
        var request = new OAuth2Request(
                null,
                claims.getClientId(),
                authorities,
                true,
                Set.of(claims.getScopes()),
                null,
                null,
                null,
                null);

        // Create the token object with the actual access token
        var token = new UsernamePasswordAuthenticationToken(claims, null, authorities);

        // Create the authentication object and set custom claims as details
        return new OAuth2Authentication(request, token);
    }
}
