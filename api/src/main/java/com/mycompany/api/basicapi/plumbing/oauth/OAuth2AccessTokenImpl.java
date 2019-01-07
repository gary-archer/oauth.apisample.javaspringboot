package com.mycompany.api.basicapi.plumbing.oauth;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;

public class OAuth2AccessTokenImpl implements OAuth2AccessToken {
    private JsonObject token;
    private String tokenString;
    private Set<String> scopes = new HashSet();
    private Date expireDate;

    public OAuth2AccessTokenImpl(JsonObject token, String tokenString) {
        this.token = token;
        this.tokenString = tokenString;
        if (token.get("scope") != null) {
            this.scopes = Sets.newHashSet(Splitter.on(" ").split(token.get("scope").getAsString()));
        }

        // HACK
        /*
        DateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        if (token.get("exp") != null) {

            this.expireDate = dateFormater.parse(token.get("exp").getAsString());
            this.expireDate = new Date();
            this.expireDate = new Date(this.expireDate.getTime() + 60L * 60L * 1000L);
        }*/

        this.expireDate = new Date();
        this.expireDate = new Date(this.expireDate.getTime() + 60L * 60L * 1000L);
    }

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
        return this.tokenString;
    }
}
