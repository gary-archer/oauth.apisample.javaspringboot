package com.mycompany.api.basicapi.plumbing.oauth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import java.util.*;

/*
 * The main middleware class for handling token validation and claims lookup
 */
public class ClaimsService implements ResourceServerTokenServices {

    /*
     * Load the credentials for the specified access token
     */
    @Override
    public OAuth2Authentication loadAuthentication(String accessToken) throws AuthenticationException, InvalidTokenException {

        System.out.println("*** Started loadAuthentication handling");

        // Hard code these for now
        String clientId = "0oac5m78icnW3C06L0h7";
        String userId = "00uc5txme5djp51gG0h7";

        // Create the principal and indicate that the user is authenticated
        Authentication authentication = new PreAuthenticatedAuthenticationToken(userId, accessToken);
        authentication.setAuthenticated(true);

        // Set hard coded scopes
        Set<String> scopes = new HashSet();
        scopes.add("openid");
        scopes.add("email");
        scopes.add("profile");

        // Create the stored request
        Map<String, String> parameters = new HashMap();
        parameters.put("client_id", clientId);
        parameters.put("scope", "openid email profile");
        OAuth2Request storedRequest = new OAuth2Request(parameters, clientId, (Collection)null, true, scopes, (Set)null, (String)null, (Set)null, (Map)null);

        System.out.println("*** Finished loadAuthentication handling");

        // Return the authentication object
        return new OAuth2Authentication(storedRequest, authentication);
    }

    /*
     * Retrieve full claims details from just the token value
     */
    @Override
    public OAuth2AccessToken readAccessToken(String accessToken) {

        System.out.println("*** Reading access token and returning claims");

        return new ApiClaims(accessToken);
    }
}
