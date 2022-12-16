package com.mycompany.sample.plumbing.oauth.claimsCaching;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.codec.digest.DigestUtils;
import com.mycompany.sample.plumbing.claims.CachedClaims;
import com.mycompany.sample.plumbing.claims.ClaimsPrincipal;
import com.mycompany.sample.plumbing.claims.ClaimsReader;
import com.mycompany.sample.plumbing.claims.CustomClaimsProvider;
import com.mycompany.sample.plumbing.errors.ErrorFactory;
import com.mycompany.sample.plumbing.oauth.Authorizer;
import com.mycompany.sample.plumbing.oauth.BearerToken;
import com.mycompany.sample.plumbing.oauth.OAuthAuthenticator;

/*
 * An authorizer that manages claims in an extensible manner, with the ability to use claims from the API's own data
 */
public final class ClaimsCachingAuthorizer implements Authorizer {

    private final ClaimsCache cache;
    private final OAuthAuthenticator authenticator;
    private final UserInfoClient userInfoClient;
    private final CustomClaimsProvider customClaimsProvider;

    public ClaimsCachingAuthorizer(
            final ClaimsCache cache,
            final OAuthAuthenticator authenticator,
            final UserInfoClient userInfoClient,
            final CustomClaimsProvider customClaimsProvider) {

        this.cache = cache;
        this.authenticator = authenticator;
        this.userInfoClient = userInfoClient;
        this.customClaimsProvider = customClaimsProvider;
    }

    /*
     * Do the OAuth processing by using token claims and then looking up other claims
     */
    @Override
    public ClaimsPrincipal execute(final HttpServletRequest request) {

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
            return new ClaimsPrincipal(baseClaims, cachedClaims.getUserInfo(), cachedClaims.getCustom());
        }

        // In Cognito we cannot issue custom claims so the API looks them up when the access token is first received
        var userInfo = this.userInfoClient.getUserInfo(accessToken);
        var customClaims = customClaimsProvider.get(accessToken, baseClaims, userInfo);
        var claimsToCache = new CachedClaims(userInfo, customClaims);

        // Cache the extra claims for subsequent requests with the same access token
        this.cache.setExtraUserClaims(accessTokenHash, claimsToCache, baseClaims.getExpiry());

        // Return the final claims
        return new ClaimsPrincipal(baseClaims, userInfo, customClaims);
    }
}
