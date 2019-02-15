package com.mycompany.api.basicapi.framework.oauth;

import com.mycompany.api.basicapi.configuration.Configuration;
import org.apache.commons.codec.digest.DigestUtils;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.cache2k.event.CacheEntryExpiredListener;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/*
 * A singleton in memory claims cache for our API
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ClaimsCache<TClaims extends CoreApiClaims> {

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
    private Cache<String, TClaims> cache;

    /*
     * Create the cache at application startup
     */
    @PostConstruct
    public void init() {

        // Output expiry debug messages here if required
        var listener = (CacheEntryExpiredListener<String, TClaims>) (cache, cacheEntry) -> {
        };

        // Create the cache with a default token expiry time
        this.cache = new Cache2kBuilder<String, TClaims>() {}
                .name("claims")
                .expireAfterWrite(this.configuration.getDefaultTokenCacheMinutes(), TimeUnit.MINUTES)
                .addListener(listener)
                .build();
    }

    /*
     * Add claims to the cache mapped to this token's hash, until the token's expiry time, which is a UTC value
     * Almost simultaneous requests from the same user could call put so the cache must be thread safe
     */
    public void addClaimsForToken(String accessToken, long tokenUtcExpirySeconds, TClaims claims) {

        // Convert to JSON
        var tokenHash = DigestUtils.sha256Hex(accessToken);

        // Add to the cache
        cache.invoke(tokenHash, e -> e.setValue(claims).setExpiryTime(tokenUtcExpirySeconds * 1000));
    }

    /*
     * Get claims from the cache for this token's hash, or return null if not found
     * Almost simultaneous requests from the same user could return null for the same token
     */
    public TClaims getClaimsForToken(String accessToken) {

        // Get the token hash and try to find existing claims
        var tokenHash = DigestUtils.sha256Hex(accessToken);
        var claims = cache.get(tokenHash);

        // Indicate not found amd that a claims lookup is needed for the new token
        if(claims == null) {
            return null;
        }

        // Return the claims object
        return claims;
    }
}
