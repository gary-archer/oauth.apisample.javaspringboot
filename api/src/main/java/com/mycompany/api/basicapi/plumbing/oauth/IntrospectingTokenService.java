package com.mycompany.api.basicapi.plumbing.oauth;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jose.util.Base64;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.http.impl.client.DefaultHttpClient;
import org.mitre.oauth2.introspectingfilter.service.IntrospectionAuthorityGranter;
import org.mitre.oauth2.introspectingfilter.service.IntrospectionConfigurationService;
import org.mitre.oauth2.introspectingfilter.service.impl.SimpleIntrospectionAuthorityGranter;
import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.oauth2.model.ClientDetailsEntity.AuthMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/*
 * A temporary copy of the mitre class for debugging
 */
public class IntrospectingTokenService implements ResourceServerTokenServices {
    private IntrospectionConfigurationService introspectionConfigurationService;
    private IntrospectionAuthorityGranter introspectionAuthorityGranter = new SimpleIntrospectionAuthorityGranter();
    private DefaultHttpClient httpClient = new DefaultHttpClient();
    private HttpComponentsClientHttpRequestFactory factory;
    private Map<String, IntrospectingTokenService.TokenCacheObject> authCache;
    private static Logger logger = LoggerFactory.getLogger(IntrospectingTokenService.class);

    public IntrospectingTokenService() {
        this.factory = new HttpComponentsClientHttpRequestFactory(this.httpClient);
        this.authCache = new HashMap();
    }

    public IntrospectionConfigurationService getIntrospectionConfigurationService() {
        return this.introspectionConfigurationService;
    }

    public void setIntrospectionConfigurationService(IntrospectionConfigurationService introspectionUrlProvider) {
        this.introspectionConfigurationService = introspectionUrlProvider;
    }

    private IntrospectingTokenService.TokenCacheObject checkCache(String key) {
        if (this.authCache.containsKey(key)) {
            IntrospectingTokenService.TokenCacheObject tco = (IntrospectingTokenService.TokenCacheObject)this.authCache.get(key);
            if (tco.token.getExpiration().after(new Date())) {
                return tco;
            }

            this.authCache.remove(key);
        }

        return null;
    }

    private OAuth2Request createStoredRequest(JsonObject token) {
        String clientId = token.get("client_id").getAsString();
        Set<String> scopes = new HashSet();

        // HACK
        /*Iterator i$ = token.get("scope").getAsJsonArray().iterator();
        while(i$.hasNext()) {
            JsonElement e = (JsonElement)i$.next();
            scopes.add(e.getAsString());
        }*/
        scopes.add("openid");
        scopes.add("email");
        scopes.add("profile");

        Map<String, String> parameters = new HashMap();
        parameters.put("client_id", clientId);
        parameters.put("scope", OAuth2Utils.formatParameterList(scopes));
        OAuth2Request storedRequest = new OAuth2Request(parameters, clientId, (Collection)null, true, scopes, (Set)null, (String)null, (Set)null, (Map)null);
        return storedRequest;
    }

    private Authentication createAuthentication(JsonObject token) {
        return new PreAuthenticatedAuthenticationToken(token.get("sub").getAsString(), token, this.introspectionAuthorityGranter.getAuthorities(token));
    }

    private OAuth2AccessToken createAccessToken(JsonObject token, String tokenString) {
        OAuth2AccessToken accessToken = new OAuth2AccessTokenImpl(token, tokenString);
        return accessToken;
    }

    private boolean parseToken(String accessToken) {
        String introspectionUrl;
        RegisteredClient client;
        try {
            introspectionUrl = this.introspectionConfigurationService.getIntrospectionUrl(accessToken);
            client = this.introspectionConfigurationService.getClientConfiguration(accessToken);
        } catch (IllegalArgumentException var14) {
            logger.error("Unable to load introspection URL or client configuration", var14);
            return false;
        }

        String validatedToken = null;
        MultiValueMap<String, String> form = new LinkedMultiValueMap();
        final String clientId = client.getClientId();
        final String clientSecret = client.getClientSecret();
        RestTemplate restTemplate;
        if (AuthMethod.SECRET_BASIC.equals(client.getTokenEndpointAuthMethod())) {
            restTemplate = new RestTemplate(this.factory) {
                protected ClientHttpRequest createRequest(URI url, HttpMethod method) throws IOException {
                    ClientHttpRequest httpRequest = super.createRequest(url, method);
                    httpRequest.getHeaders().add("Authorization", String.format("Basic %s", Base64.encode(String.format("%s:%s", clientId, clientSecret))));
                    return httpRequest;
                }
            };
        } else {
            restTemplate = new RestTemplate(this.factory);
            form.add("client_id", clientId);
            form.add("client_secret", clientSecret);
        }

        form.add("token", accessToken);

        try {
            validatedToken = (String)restTemplate.postForObject(introspectionUrl, form, String.class, new Object[0]);
        } catch (RestClientException var13) {
            logger.error("validateToken", var13);
        }

        if (validatedToken != null) {
            JsonElement jsonRoot = (new JsonParser()).parse(validatedToken);
            if (!jsonRoot.isJsonObject()) {
                return false;
            }

            JsonObject tokenResponse = jsonRoot.getAsJsonObject();
            if (tokenResponse.get("error") != null) {
                logger.error("Got an error back: " + tokenResponse.get("error") + ", " + tokenResponse.get("error_description"));
                return false;
            }

            if (!tokenResponse.get("active").getAsBoolean()) {
                logger.info("Server returned non-active token");
                return false;
            }

            OAuth2Authentication auth = new OAuth2Authentication(this.createStoredRequest(tokenResponse), this.createAuthentication(tokenResponse));
            OAuth2AccessToken token = this.createAccessToken(tokenResponse, accessToken);
            if (token.getExpiration().after(new Date())) {
                this.authCache.put(accessToken, new IntrospectingTokenService.TokenCacheObject(token, auth));
                return true;
            }
        }

        return false;
    }

    public OAuth2Authentication loadAuthentication(String accessToken) throws AuthenticationException {

        System.out.println("*** DEBUG: loadAuthentication end");

        IntrospectingTokenService.TokenCacheObject cacheAuth = this.checkCache(accessToken);
        if (cacheAuth != null) {

            System.out.println("*** DEBUG: loadAuthentication: returning from cache");
            return cacheAuth.auth;
        } else if (this.parseToken(accessToken)) {

            System.out.println("*** DEBUG: loadAuthentication: introspecting");
            cacheAuth = (IntrospectingTokenService.TokenCacheObject)this.authCache.get(accessToken);
            return cacheAuth != null && cacheAuth.token.getExpiration().after(new Date()) ? cacheAuth.auth : null;
        } else {

            System.out.println("*** DEBUG: loadAuthentication: returning null");
            return null;
        }
    }

    public OAuth2AccessToken readAccessToken(String accessToken) {

        System.out.println("*** DEBUG: readAccessToken start");

        IntrospectingTokenService.TokenCacheObject cacheAuth = this.checkCache(accessToken);
        if (cacheAuth != null) {

            System.out.println("*** DEBUG: readAccessToken: returning from cache");
            return cacheAuth.token;
        } else if (this.parseToken(accessToken)) {

            System.out.println("*** DEBUG: readAccessToken: introspecting");
            cacheAuth = (IntrospectingTokenService.TokenCacheObject)this.authCache.get(accessToken);
            return cacheAuth != null && cacheAuth.token.getExpiration().after(new Date()) ? cacheAuth.token : null;
        } else {

            System.out.println("*** DEBUG: readAccessToken: returning null");
            return null;
        }
    }

    private class TokenCacheObject {
        OAuth2AccessToken token;
        OAuth2Authentication auth;

        private TokenCacheObject(OAuth2AccessToken token, OAuth2Authentication auth) {
            this.token = token;
            this.auth = auth;
        }
    }
}
