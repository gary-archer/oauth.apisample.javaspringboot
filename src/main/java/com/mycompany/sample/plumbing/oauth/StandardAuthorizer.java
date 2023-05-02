package com.mycompany.sample.plumbing.oauth;

import jakarta.servlet.http.HttpServletRequest;
import com.mycompany.sample.plumbing.claims.ClaimsPrincipal;
import com.mycompany.sample.plumbing.claims.ClaimsReader;
import com.mycompany.sample.plumbing.claims.CustomClaimsProvider;
import com.mycompany.sample.plumbing.errors.ErrorFactory;

/*
 * An authorizer that relies on the advanced features of the Authorization Server to provide claims
 * This is the preferred option when supported, since it leads to simpler code and better security
 */
public final class StandardAuthorizer implements Authorizer {

    private final OAuthAuthenticator authenticator;
    private final CustomClaimsProvider customClaimsProvider;

    public StandardAuthorizer(
            final OAuthAuthenticator authenticator,
            final CustomClaimsProvider customClaimsProvider) {

        this.authenticator = authenticator;
        this.customClaimsProvider = customClaimsProvider;
    }

    /*
     * Do the OAuth processing by using only token claims
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

        // Then read all claims from the token
        var baseClaims = ClaimsReader.baseClaims(payload);
        var userInfo = ClaimsReader.userInfoClaims(payload);
        var customClaims = this.customClaimsProvider.getFromPayload(payload);
        return new ClaimsPrincipal(baseClaims, userInfo, customClaims);
    }
}
