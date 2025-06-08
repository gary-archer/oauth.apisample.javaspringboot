package com.authsamples.api.plumbing.oauth;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.authsamples.api.plumbing.claims.ClaimsCache;
import com.authsamples.api.plumbing.claims.ClaimsPrincipal;
import com.authsamples.api.plumbing.claims.ClaimsReader;
import com.authsamples.api.plumbing.claims.ExtraClaimsProvider;
import com.authsamples.api.plumbing.dependencies.CustomRequestScope;
import com.authsamples.api.plumbing.errors.ErrorFactory;

/*
 * A class to create the claims principal at the start of every API request
 */
@Component
@Scope(value = CustomRequestScope.NAME)
public final class OAuthFilter {

    private final ClaimsCache cache;
    private final AccessTokenValidator tokenValidator;
    private final ExtraClaimsProvider extraClaimsProvider;

    public OAuthFilter(
            final ClaimsCache cache,
            final AccessTokenValidator tokenValidator,
            final ExtraClaimsProvider extraClaimsProvider) {

        this.cache = cache;
        this.tokenValidator = tokenValidator;
        this.extraClaimsProvider = extraClaimsProvider;
    }

    /*
     * Validate the OAuth access token and then look up other values
     */
    public ClaimsPrincipal execute(final HttpServletRequest request) {

        // First read the access token
        String accessToken = BearerToken.read(request);
        if (accessToken == null) {
            throw ErrorFactory.createClient401Error("No access token was supplied in the bearer header");
        }

        // Validate the JWT access token on every API request, in a zero trust manner
        var jwtClaims = this.tokenValidator.execute(accessToken);

        // If cached extra values already exist for this token then return them immediately
        String accessTokenHash = DigestUtils.sha256Hex(accessToken);
        var extraClaims = this.cache.getItem(accessTokenHash);
        if (extraClaims != null) {
            return new ClaimsPrincipal(jwtClaims, extraClaims);
        }

        // Look up extra authorization values not in the JWT
        extraClaims = this.extraClaimsProvider.lookupExtraClaims(jwtClaims);

        // Cache the extra values for subsequent requests with the same access token
        this.cache.setItem(accessTokenHash, extraClaims, ClaimsReader.getExpiryClaim(jwtClaims));

        // Return the final claims used by the API's authorization logic
        return new ClaimsPrincipal(jwtClaims, extraClaims);
    }
}
