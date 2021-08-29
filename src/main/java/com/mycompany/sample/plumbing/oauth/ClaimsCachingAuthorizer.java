package com.mycompany.sample.plumbing.oauth;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.digest.DigestUtils;
import com.mycompany.sample.plumbing.claims.ApiClaims;
import com.mycompany.sample.plumbing.claims.ClaimsCache;
import com.mycompany.sample.plumbing.claims.ClaimsProvider;
import com.mycompany.sample.plumbing.errors.ErrorFactory;

/*
 * An authorizer that manages claims in an extensible manner, with the ability to use claims from the API's own data
 */
public final class ClaimsCachingAuthorizer implements Authorizer {

    private final ClaimsCache cache;
    private final OAuthAuthenticator authenticator;
    private final ClaimsProvider customClaimsProvider;

    public ClaimsCachingAuthorizer(
            final ClaimsCache cache,
            final OAuthAuthenticator authenticator,
            final ClaimsProvider customClaimsProvider) {

        this.cache = cache;
        this.authenticator = authenticator;
        this.customClaimsProvider = customClaimsProvider;
    }

    /*
     * OAuth authorization involves token validation and claims lookup
     */
    @Override
    public ApiClaims execute(final HttpServletRequest request) {

        // First read the access token
        String accessToken = BearerToken.read(request);
        if (accessToken == null) {
            throw ErrorFactory.createClient401Error("No access token was supplied in the bearer header");
        }

        // If cached results already exist for this token then return them immediately
        String accessTokenHash = DigestUtils.sha256Hex(accessToken);
        var cachedClaims = this.cache.getClaimsForToken(accessTokenHash);
        if (cachedClaims != null) {
            return cachedClaims;
        }

        // Validate the token and read token claims
        var baseClaims = this.authenticator.validateToken(accessToken);

        // Do the work for user info lookup
        var userInfoClaims = this.authenticator.getUserInfo(accessToken);

        // Ask the claims provider to create the final claims object
        var claims = this.customClaimsProvider.supplyClaims(baseClaims, userInfoClaims);

        // Cache the claims against the token hash until the token's expiry time
        this.cache.addClaimsForToken(accessTokenHash, claims);
        return claims;
    }
}
