package com.mycompany.sample.plumbing.oauth;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.mycompany.sample.plumbing.claims.ApiClaims;
import com.mycompany.sample.plumbing.claims.ClaimsCache;
import com.mycompany.sample.plumbing.claims.CustomClaimsProvider;
import com.mycompany.sample.plumbing.dependencies.CustomRequestScope;
import com.mycompany.sample.plumbing.errors.ErrorFactory;
import com.mycompany.sample.plumbing.logging.LogEntryImpl;

/*
 * An authorizer that manages claims in an extensible manner, with the ability to use claims from the API's own data
 */
@Component
@Scope(value = CustomRequestScope.NAME)
public final class ClaimsCachingAuthorizer implements Authorizer {

    private final ClaimsCache cache;
    private final OAuthAuthenticator authenticator;
    private final CustomClaimsProvider customClaimsProvider;
    private final LogEntryImpl logEntry;

    public ClaimsCachingAuthorizer(
            final ClaimsCache cache,
            final OAuthAuthenticator authenticator,
            final CustomClaimsProvider customClaimsProvider,
            final LogEntryImpl logEntry) {

        this.cache = cache;
        this.authenticator = authenticator;
        this.customClaimsProvider = customClaimsProvider;
        this.logEntry = logEntry;
    }

    /*
     * OAuth authorization involves token validation and claims lookup
     */
    @Override
    public ApiClaims execute(final HttpServletRequest request) {

        // First read the access token
        String accessToken = this.readAccessToken(request);
        if (accessToken == null) {
            throw ErrorFactory.createClient401Error("No access token was supplied in the bearer header");
        }

        // If cached results already exist for this token then return them immediately
        String accessTokenHash = DigestUtils.sha256Hex(accessToken);
        var cachedClaims = this.cache.getClaimsForToken(accessTokenHash);
        if (cachedClaims != null) {
            return cachedClaims;
        }

        // Create a child log entry for authentication related work
        // This ensures that any errors and performances in this area are reported separately to business logic
        var authorizationLogEntry = this.logEntry.createChild("authorizer");

        // Validate the token and read token claims
        var baseClaims = this.authenticator.validateToken(accessToken);

        // Do the work for user info lookup
        var userInfoClaims = this.authenticator.getUserInfo(accessToken);

        // Get custom claims from the API's own data if needed
        var claims = this.customClaimsProvider.supplyClaims(baseClaims, userInfoClaims);

        // Cache the claims against the token hash until the token's expiry time
        this.cache.addClaimsForToken(accessTokenHash, claims);

        // Finish logging here, and on exception the child is disposed by logging classes
        authorizationLogEntry.close();
        return claims;
    }

    /*
     * Try to read the access token from the authorization header
     */
    private String readAccessToken(final HttpServletRequest request) {

        var header = request.getHeader("Authorization");
        if (header != null) {
            var parts = header.split(" ");
            if (parts.length == 2 && parts[0].equalsIgnoreCase("Bearer")) {
                return parts[1];
            }
        }

        return null;
    }
}
