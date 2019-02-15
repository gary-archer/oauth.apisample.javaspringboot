package com.mycompany.api.basicapi.framework.oauth;

import com.mycompany.api.basicapi.framework.errors.ErrorHandler;
import com.mycompany.api.basicapi.framework.utilities.ClaimsFactory;
import com.mycompany.api.basicapi.framework.utilities.ResponseWriter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

/*
 * The Spring entry point for handling token validation and claims lookup
 */
public class AuthorizationFilter<TClaims extends CoreApiClaims> extends OncePerRequestFilter {

    /*
     * Injected dependencies
     */
    private final OauthConfiguration configuration;
    private final IssuerMetadata metadata;
    private final ClaimsCache cache;
    private final ClaimsFactory claimsFactory;
    private final String[] trustedOrigins;

    /*
     * Receive our JSON configuration
     */
    public AuthorizationFilter(
            OauthConfiguration configuration,
            IssuerMetadata metadata,
            ClaimsCache cache,
            ClaimsFactory claimsFactory,
            String[] trustedOrigins)
    {
        this.configuration = configuration;
        this.metadata = metadata;
        this.cache = cache;
        this.claimsFactory = claimsFactory;
        this.trustedOrigins = trustedOrigins;
    }

    /*
     * Get the access token from the authorization header and manage token validation and claims lookup when required
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {

        try {

            // Create authorization related classes on every API request
            var authenticator = new Authenticator(this.configuration, this.metadata);
            var claimsMiddleware = new ClaimsMiddleware(this.cache, authenticator, this.claimsFactory);

            // Try to get the access token and create empty claims
            String accessToken = this.readToken(request);

            // Try to process the token and get claims
            var claims = claimsMiddleware.authorizeRequestAndGetClaims(accessToken);
            if(claims != null) {

                // Update the Spring security context with claims and move on to business logic
                SecurityContextHolder.getContext().setAuthentication(this.createOAuth2Authentication(claims));
                filterChain.doFilter(request, response);
            }
            else {

                // Non success responses mean a missing, expired or invalid token, and we will return 401
                var writer = new ResponseWriter(this.trustedOrigins);
                writer.writeInvalidTokenResponse(response);
            }

        } catch(Exception ex) {

            // Any failures will be thrown as exceptions and will result in a 500 response
            var handler = new ErrorHandler();
            handler.handleFilterException(response, ex, this.trustedOrigins);
        }
    }

    /*
     * Read the access token
     */
    private String readToken(HttpServletRequest request) {

        // Get the received access token
        var extractor = new BearerTokenExtractor();
        var token = extractor.extract(request);
        if(token != null) {
            return token.getPrincipal().toString();
        }

        return null;
    }

    /*
     * Plumbing to update Spring Boot's security objects
     */
    private OAuth2Authentication createOAuth2Authentication(CoreApiClaims claims)
    {
        // Spring security requires a granted authorities list based on roles, whereas this sample uses claims based security
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
