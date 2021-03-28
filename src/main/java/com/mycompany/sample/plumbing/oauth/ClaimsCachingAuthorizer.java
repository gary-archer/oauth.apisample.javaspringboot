package com.mycompany.sample.plumbing.oauth;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.digest.DigestUtils;
import com.mycompany.sample.plumbing.claims.ApiClaims;
import com.mycompany.sample.plumbing.claims.ClaimsCache;
import com.mycompany.sample.plumbing.claims.CustomClaimsProvider;
import com.mycompany.sample.plumbing.errors.ErrorFactory;

/*
 * An authorizer that manages claims in an extensible manner, with the ability to use claims from the API's own data
 */
public final class ClaimsCachingAuthorizer implements Authorizer {

    private final ClaimsCache _cache;
    private final OAuthAuthenticator _authenticator;
    private final CustomClaimsProvider _customClaimsProvider;

    public ClaimsCachingAuthorizer(
            final ClaimsCache cache,
            final OAuthAuthenticator authenticator,
            final CustomClaimsProvider customClaimsProvider) {

        this._cache = cache;
        this._authenticator = authenticator;
        this._customClaimsProvider = customClaimsProvider;
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
        var cachedClaims = this._cache.getClaimsForToken(accessTokenHash);
        if (cachedClaims != null) {
            return cachedClaims;
        }

        // Validate the token and read token claims
        var baseClaims = this._authenticator.validateToken(accessToken);

        // Do the work for user info lookup
        var userInfoClaims = this._authenticator.getUserInfo(accessToken);

        // Get custom claims from the API's own data if needed
        var claims = this._customClaimsProvider.supplyClaims(baseClaims, userInfoClaims);

        // Cache the claims against the token hash until the token's expiry time
        this._cache.addClaimsForToken(accessTokenHash, claims);
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
