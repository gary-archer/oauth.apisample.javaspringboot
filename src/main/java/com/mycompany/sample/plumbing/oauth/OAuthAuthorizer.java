package com.mycompany.sample.plumbing.oauth;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.mycompany.sample.plumbing.claims.ClaimsCache;
import com.mycompany.sample.plumbing.claims.ClaimsPrincipal;
import com.mycompany.sample.plumbing.claims.ClaimsReader;
import com.mycompany.sample.plumbing.claims.CustomClaimsProvider;
import com.mycompany.sample.plumbing.dependencies.CustomRequestScope;
import com.mycompany.sample.plumbing.errors.ErrorFactory;

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
        var jwtClaims = this.authenticator.validateToken(accessToken);

        // If cached results already exist for this token then return them immediately
        String accessTokenHash = DigestUtils.sha256Hex(accessToken);
        var customClaims = this.cache.getExtraUserClaims(accessTokenHash);
        if (customClaims != null) {
            return new ClaimsPrincipal(jwtClaims, customClaims);
        }

        // Look up custom claims not in the JWT access token when it is first received
        customClaims = customClaimsProvider.lookupForNewAccessToken(accessToken, jwtClaims);

        // Cache the extra claims for subsequent requests with the same access token
        this.cache.setExtraUserClaims(accessTokenHash, customClaims, ClaimsReader.getExpiryClaim(jwtClaims));

        // Return the final claims
        return new ClaimsPrincipal(jwtClaims, customClaims);
    }
}
