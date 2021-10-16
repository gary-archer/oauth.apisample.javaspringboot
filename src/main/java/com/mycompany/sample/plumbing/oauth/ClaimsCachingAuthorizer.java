package com.mycompany.sample.plumbing.oauth;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.digest.DigestUtils;
import com.mycompany.sample.plumbing.claims.ApiClaims;
import com.mycompany.sample.plumbing.claims.CachedClaims;
import com.mycompany.sample.plumbing.claims.ClaimsCache;
import com.mycompany.sample.plumbing.claims.ClaimsProvider;
import com.mycompany.sample.plumbing.claims.ClaimsReader;
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
     * Do the OAuth processing by using token claims and then looking up other claims
     */
    @Override
    public ApiClaims execute(final HttpServletRequest request) {

        // First read the access token
        String accessToken = BearerToken.read(request);
        if (accessToken == null) {
            throw ErrorFactory.createClient401Error("No access token was supplied in the bearer header");
        }

        // On every API request we validate the JWT, in a zero trust manner
        var payload = this.authenticator.validateToken(accessToken);
        var baseClaims = ClaimsReader.baseClaims(payload);

        // If cached results already exist for this token then return them immediately
        String accessTokenHash = DigestUtils.sha256Hex(accessToken);
        var cachedClaims = this.cache.getExtraUserClaims(accessTokenHash);
        if (cachedClaims != null) {
            return new ApiClaims(baseClaims, cachedClaims.getUserInfo(), cachedClaims.getCustom());
        }

        // In Cognito we cannot issue custom claims so the API looks them up when the access token is first received
        var userInfo = this.authenticator.getUserInfo(accessToken);
        var customClaims = customClaimsProvider.get(accessToken, baseClaims, userInfo);
        var claimsToCache = new CachedClaims(userInfo, customClaims);

        // Cache the extra claims for subsequent requests with the same access token
        this.cache.setExtraUserClaims(accessTokenHash, claimsToCache, baseClaims.getExpiry());

        // Return the final claims
        return new ApiClaims(baseClaims, userInfo, customClaims);
    }
}
