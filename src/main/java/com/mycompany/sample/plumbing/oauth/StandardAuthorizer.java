package com.mycompany.sample.plumbing.oauth;

import javax.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.mycompany.sample.plumbing.claims.ApiClaims;
import com.mycompany.sample.plumbing.claims.CustomClaimsProvider;
import com.mycompany.sample.plumbing.dependencies.CustomRequestScope;
import com.mycompany.sample.plumbing.errors.ErrorFactory;

/*
 * An authorizer that relies on the advanced features of the Authorization Server to provide claims
 * This is the preferred option when supported, since it leads to simpler code and better security
 */
@Component
@Scope(value = CustomRequestScope.NAME)
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
     * OAuth authorization involves token validation and claims lookup
     */
    @Override
    public ApiClaims execute(final HttpServletRequest request) {

        // First read the access token
        String accessToken = this.readAccessToken(request);
        if (accessToken == null) {
            throw ErrorFactory.createClient401Error("No access token was supplied in the bearer header");
        }

        // Read all claims from the access token, including custom claims and those with user info
        var payload = this.authenticator.validateToken(accessToken);
        return this.customClaimsProvider.readClaims(payload);
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
