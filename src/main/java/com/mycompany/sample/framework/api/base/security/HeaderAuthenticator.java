package com.mycompany.sample.framework.api.base.security;

import com.mycompany.sample.framework.api.base.errors.ErrorUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import javax.servlet.http.HttpServletRequest;

/*
 * An alternative authenticator for private APIs that reads headers supplied by a public API
 */
@Component
@RequestScope
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class HeaderAuthenticator {

    /*
     * This form of authentication just reads claims from custom headers
     */
    public CoreApiClaims authorizeRequestAndGetClaims(final HttpServletRequest request) {

        var claims = new CoreApiClaims();

        // Get token claims
        var userId = this.getHeaderClaim(request, "'x-mycompany-user-id");
        var clientId = this.getHeaderClaim(request, "x-mycompany-client-id");
        var scope = this.getHeaderClaim(request, "x-mycompany-scope");

        // Get user info claims
        var givenName = this.getHeaderClaim(request, "x-mycompany-given-name");
        var familyName = this.getHeaderClaim(request, "x-mycompany-family-name");
        var email = this.getHeaderClaim(request, "x-mycompany-email");

        // Update the claims object
        claims.setTokenInfo(userId, clientId, scope.split(" "));
        claims.setCentralUserInfo(givenName, familyName, email);
        return claims;
    }

    /*
     * Try to read a claim from custom request headers
     */
    private String getHeaderClaim(final HttpServletRequest request, final String claimName) {

        var result = request.getHeader(claimName);
        if (result == null) {
            throw ErrorUtils.fromMissingClaim(claimName);
        }

        return result;
    }
}
