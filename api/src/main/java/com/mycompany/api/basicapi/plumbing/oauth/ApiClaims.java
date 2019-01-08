package com.mycompany.api.basicapi.plumbing.oauth;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;

/*
 * Use the Spring Boot mechanism to supply claims to API controllers
 */
public class ApiClaims implements OAuth2AccessToken {

    /*
     * Our claims include token claims, central user data and product specific user data
     */
    private String accessToken;
    private Set<String> scopes;
    private Date expireDate;

    /*
     * Receive data during claims handling
     */
    public ApiClaims(String accessToken) {

        // Store the access token
        this.accessToken = accessToken;

        // Hard code these for now
        String clientId = "0oac5m78icnW3C06L0h7";
        String userId = "00uc5txme5djp51gG0h7";

        // Hard code some scopes for now
        this.scopes = new HashSet();
        this.scopes.add("openid");
        this.scopes.add("email");
        this.scopes.add("profile");

        // Hard code the expiry date to 30 minutes in the future
        this.expireDate = new Date(new Date().getTime() + 30L * 60L * 1000L);

        System.out.println("*** Created Api Claims");
    }

    /*
     * Interface methods
     */
    public Map<String, Object> getAdditionalInformation() {
        return null;
    }

    public Set<String> getScope() {
        return this.scopes;
    }

    public OAuth2RefreshToken getRefreshToken() {
        return null;
    }

    public String getTokenType() {
        return "Bearer";
    }

    public boolean isExpired() {
        return this.expireDate != null && this.expireDate.before(new Date());
    }

    public Date getExpiration() {
        return this.expireDate;
    }

    public int getExpiresIn() {
        return this.expireDate != null ? (int)TimeUnit.MILLISECONDS.toSeconds(this.expireDate.getTime() - (new Date()).getTime()) : 0;
    }

    public String getValue() {
        return this.accessToken;
    }
}
