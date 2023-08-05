package com.mycompany.sample.plumbing.oauth;

import com.mycompany.sample.plumbing.dependencies.CustomRequestScope;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.codec.digest.DigestUtils;
import com.mycompany.sample.plumbing.claims.CachedClaims;
import com.mycompany.sample.plumbing.claims.ClaimsPrincipal;
import com.mycompany.sample.plumbing.claims.ClaimsReader;
import com.mycompany.sample.plumbing.claims.CustomClaimsProvider;
import com.mycompany.sample.plumbing.errors.ErrorFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/*
 * An authorizer that enables the API to add claims from its own data
 */
@Component
@Scope(value = CustomRequestScope.NAME)
public final class OAuthAuthorizer implements Authorizer {

    private final ClaimsCache cache;
    private final OAuthAuthenticator authenticator;
    private final CustomClaimsProvider customClaimsProvider;

    public OAuthAuthorizer(
            final ClaimsCache cache,
            final OAuthAuthenticator authenticator,
            final CustomClaimsProvider customClaimsProvider) {

        this.cache = cache;
        this.authenticator = authenticator;
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
            return new ClaimsPrincipal(baseClaims, cachedClaims.getCustom());
        }

        // In Cognito we cannot issue custom claims so the API looks them up when the access token is first received
        var customClaims = customClaimsProvider.lookupForNewAccessToken(accessToken, baseClaims);
        var claimsToCache = new CachedClaims(customClaims);

        // Cache the extra claims for subsequent requests with the same access token
        this.cache.setExtraUserClaims(accessTokenHash, claimsToCache, baseClaims.getExpiry());

        // Return the final claims
        return new ClaimsPrincipal(baseClaims, customClaims);
    }
}
