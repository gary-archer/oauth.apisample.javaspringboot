package com.mycompany.api.basicapi.plumbing.oauth;

import com.mycompany.api.basicapi.entities.ApiClaims;
import com.mycompany.api.basicapi.entities.UserInfoClaims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import java.util.*;

/*
 * The main middleware class for handling token validation and claims lookup
 */
public class ClaimsService implements ResourceServerTokenServices {

    /*
     * Handle claims lookup and authentication
     */
    @Override
    public OAuth2Authentication loadAuthentication(String accessToken) throws AuthenticationException, InvalidTokenException {

        /* Use of Connect2Id will replace the below hard coded claims */

        // Hard code our claims object for now
        ApiClaims claims = new ApiClaims("00uc5txme5djp51gG0h7", "0oac5m78icnW3C06L0h7", "openid email profile");
        claims.setUserInfo(new UserInfoClaims("Guest", "User", "guestuser@authguidance.com"));
        claims.setUserCompanyIds(new Integer[]{1, 2, 3});

        // Then give Spring Boot what it needs
        return this.createOAuth2Authentication(claims);
    }

    /*
     * It seems to be standard to do this
     */
    @Override
    public OAuth2AccessToken readAccessToken(String accessToken) {
        throw new UnsupportedOperationException("The readAccessToken was called unexpectedly");
    }

    /*
     * Plumbing to update Spring Boot's confusing security objects
     */
    private OAuth2Authentication createOAuth2Authentication(ApiClaims claims)
    {
        System.out.println("*** Started loadAuthentication handling");

        // Create authorities
        List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_API");
        for(String scope: claims.getScopes()) {
            authorities.add(new SimpleGrantedAuthority("OAUTH_SCOPE_" + scope));
        }

        // Create this plumbing object which Spring Boot requires
        OAuth2Request request = new OAuth2Request(
                null,
                claims.getCallingApplicationId(),
                authorities,
                true,
                claims.getScopes(),
                null,
                null,
                null,
                null);

        // Create the token object
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(claims, null, authorities);

        // Ensure that it is set against the context
        // SecurityContextHolder.getContext().setAuthentication(token);
        System.out.println("*** Ended loadAuthentication handling");

        return new OAuth2Authentication(request, token);
    }
}
