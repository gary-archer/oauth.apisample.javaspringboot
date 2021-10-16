package com.mycompany.sample.plumbing.oauth;

import javax.servlet.http.HttpServletRequest;
import com.mycompany.sample.plumbing.claims.ApiClaims;
import com.mycompany.sample.plumbing.claims.ClaimsProvider;
import com.mycompany.sample.plumbing.claims.ClaimsReader;
import com.mycompany.sample.plumbing.errors.ErrorFactory;

/*
 * An authorizer that relies on the advanced features of the Authorization Server to provide claims
 * This is the preferred option when supported, since it leads to simpler code and better security
 */
public final class StandardAuthorizer implements Authorizer {

    private final OAuthAuthenticator authenticator;
    private final ClaimsProvider claimsProvider;

    public StandardAuthorizer(
            final OAuthAuthenticator authenticator,
            final ClaimsProvider claimsProvider) {

        this.authenticator = authenticator;
        this.claimsProvider = claimsProvider;
    }

    /*
     * Do the OAuth processing by using only token claims
     */
    @Override
    public ApiClaims execute(final HttpServletRequest request) {

        // First read the access token
        String accessToken = BearerToken.read(request);
        if (accessToken == null) {
            throw ErrorFactory.createClient401Error("No access token was supplied in the bearer header");
        }

        // Do the token validation work
        var payload = this.authenticator.validateToken(accessToken);

        // Then read all claims from the token
        var baseClaims = ClaimsReader.baseClaims(payload);
        var userInfo = ClaimsReader.userInfoClaims(payload);
        var customClaims = this.claimsProvider.get(accessToken, baseClaims, userInfo);
        return new ApiClaims(baseClaims, userInfo, customClaims);
    }
}
