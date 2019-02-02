package com.mycompany.api.basicapi.plumbing.oauth;

import com.mycompany.api.basicapi.configuration.Configuration;
import com.mycompany.api.basicapi.entities.BasicApiClaims;
import com.mycompany.api.basicapi.logic.AuthorizationRulesRepository;
import com.mycompany.api.basicapi.plumbing.errors.ErrorHandler;
import com.mycompany.api.basicapi.plumbing.utilities.ResponseWriter;
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
import java.util.function.Function;
import java.util.function.Supplier;

/*
 * The Spring entry point for handling token validation and claims lookup
 */
public class AuthorizationFilter extends OncePerRequestFilter {

    /*
     * The injected configuration
     */
    private final Configuration configuration;
    private final IssuerMetadata metadata;
    private final ClaimsCache cache;
    private final Supplier<CoreApiClaims> claimsSupplier;

    /*
     * Receive our JSON configuration
     */
    public AuthorizationFilter(Configuration configuration, IssuerMetadata metadata, ClaimsCache cache, Supplier<CoreApiClaims> claimsSupplier)
    {
        this.configuration = configuration;
        this.metadata = metadata;
        this.cache = cache;
        this.claimsSupplier = claimsSupplier;
    }

    /*
     * Get the access token from the authorization header and manage token validation and claims lookup when required
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {

        try {

            // Create authorization related classes on every API request
            var authenticator = new Authenticator(this.configuration.getOauth(), this.metadata);
            var rulesRepository = new AuthorizationRulesRepository();
            var claimsMiddleware = new ClaimsMiddleware(this.cache, authenticator, rulesRepository);

            // Try to get the access token and create empty claims
            String accessToken = this.readToken(request);
            var claims =  this.claimsSupplier.get();

            // Try to process the token and get claims
            var success = claimsMiddleware.authorizeRequestAndSetClaims(accessToken, claims);
            if(success) {

                // Update the Spring security context with claims and move on to business logic
                SecurityContextHolder.getContext().setAuthentication(this.createOAuth2Authentication(claims));
                filterChain.doFilter(request, response);
            }
            else {

                // Non success responses mean a missing, expired or invalid token, and we will return 401
                var writer = new ResponseWriter(this.configuration);
                writer.writeInvalidTokenResponse(response);
            }

        } catch(Exception ex) {

            // Any failures will be thrown as exceptions and will result in a 500 response
            var handler = new ErrorHandler();
            handler.handleFilterException(response, ex, this.configuration);
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
