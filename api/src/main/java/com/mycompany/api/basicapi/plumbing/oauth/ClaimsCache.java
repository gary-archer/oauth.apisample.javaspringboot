package com.mycompany.api.basicapi.plumbing.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.api.basicapi.configuration.Configuration;
import com.mycompany.api.basicapi.configuration.OauthConfiguration;
import org.apache.commons.codec.digest.DigestUtils;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.cache2k.event.CacheEntryExpiredListener;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/*
 * A singleton in memory claims cache for our API
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ClaimsCache {

    /*
     * The injected configuration
     */
    private final OauthConfiguration configuration;

    /*
     * Receive dependencies
     */
    public ClaimsCache(Configuration configuration) {
        this.configuration = configuration.getOauth();
    }

    /*
     * The singleton cache
     */
    private Cache<String, String> cache;

    /*
     * Create the cache at application startup
     */
    @PostConstruct
    public void init() {

        // Output expiry debug messages here if required
        var listener = (CacheEntryExpiredListener<String, String>) (cache, cacheEntry) -> {
        };

        // Create the cache with a default token expiry time
        this.cache = new Cache2kBuilder<String, String>() {}
                .name("claims")
                .expireAfterWrite(this.configuration.getDefaultTokenCacheMinutes(), TimeUnit.MINUTES)
                .addListener(listener)
                .build();
    }

    /*
     * Add claims to the cache mapped to this token's hash, until the token's expiry time, which is a UTC value
     * Almost simultaneous requests from the same user could call put so the cache must be thread safe
     */
    public void addClaimsForToken(String accessToken, long tokenUtcExpirySeconds, CoreApiClaims claims) {

        // Convert to JSON
        var tokenHash = DigestUtils.sha256Hex(accessToken);
        var claimsJson = claims.toJson().toString();

        // Add to the cache
        cache.invoke(
                tokenHash,
                e -> e.setValue(claimsJson)
                        .setExpiryTime(tokenUtcExpirySeconds * 1000));
    }

    /*
     * Get claims from the cache for this token's hash, or return null if not found
     * Almost simultaneous requests from the same user could return null for the same token
     */
    public boolean getClaimsForToken(String accessToken, CoreApiClaims claims) {


        var tokenHash = DigestUtils.sha256Hex(accessToken);
        var claimsJson = cache.get(tokenHash);

        // Indicate that a claims lookup is needed
        if(claimsJson == null) {
            return false;
        }

        // Let the claims object deserialize its fields
        claims.fromJson(this.deserializeClaimsJson(claimsJson));
        return true;
    }

    /*
     * Deserialize into a dynamic object
     */
    private ObjectNode deserializeClaimsJson(String claimsJson) {

        try {
            var mapper = new ObjectMapper();
            return (ObjectNode) mapper.readTree(claimsJson);
        }
        catch(IOException ex) {
            throw new RuntimeException("IOException parsing JSON into an object", ex);
        }
    }
}
