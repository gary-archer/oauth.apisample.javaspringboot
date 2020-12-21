package com.mycompany.sample.plumbing.oauth;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.mycompany.sample.plumbing.claims.ClaimsCache;
import com.mycompany.sample.plumbing.claims.ClaimsSupplier;
import com.mycompany.sample.plumbing.claims.CoreApiClaims;
import com.mycompany.sample.plumbing.dependencies.CustomRequestScope;
import com.mycompany.sample.plumbing.errors.ErrorFactory;
import com.mycompany.sample.plumbing.security.Authorizer;

/*
 * A plain Java class to manage `token validation and claims lookup
 */
@Component
@Scope(value = CustomRequestScope.NAME)
public final class OAuthAuthorizer<TClaims extends CoreApiClaims> implements Authorizer {

    private final ClaimsCache<TClaims> cache;
    private final ClaimsSupplier<TClaims> claimsSupplier;
    private final OAuthAuthenticator authenticator;

    public OAuthAuthorizer(
            final ClaimsCache<TClaims> cache,
            final ClaimsSupplier<TClaims> claimsSupplier,
            final OAuthAuthenticator authenticator) {

        this.cache = cache;
        this.claimsSupplier = claimsSupplier;
        this.authenticator = authenticator;
    }

    /*
     * OAuth authorization involves token validation and claims lookup
     */
    @Override
    public CoreApiClaims execute(final HttpServletRequest request) {

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

        // Otherwise create new claims which we will populate
        var claims = this.claimsSupplier.createEmptyClaims();

        // Add OAuth claims from introspection and user info lookup
        this.authenticator.validateTokenAndGetClaims(accessToken, request, claims);

        // Add custom claims from the API's own data if needed
        this.claimsSupplier.createCustomClaimsProvider().addCustomClaims(accessToken, request, claims);

        // Cache the claims against the token hash until the token's expiry time
        this.cache.addClaimsForToken(accessTokenHash, claims);

        // Return the result on success
        return claims;
    }

    /*
     * Try to read the access token from the authorization header
     */
    private String readAccessToken(final HttpServletRequest request) {

        var header = request.getHeader("Authorization");
        if (header != null) {
            var parts = header.split(" ");
            if (parts.length == 2 && parts[0].equals("Bearer")) {
                return parts[1];
            }
        }

        return null;
    }
}
