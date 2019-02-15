package com.mycompany.api.basicapi.framework.oauth;

import org.apache.commons.codec.digest.DigestUtils;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.cache2k.event.CacheEntryExpiredListener;
import java.util.concurrent.TimeUnit;

/*
 * A singleton in memory claims cache for our API
 */
public class ClaimsCache<TClaims extends CoreApiClaims> {

    /*
     * The injected configuration
     */
    private final OauthConfiguration configuration;

    /*
     * Receive dependencies
     */
    public ClaimsCache(OauthConfiguration configuration) {
        this.configuration = configuration;
    }

    /*
     * The singleton cache
     */
    private Cache<String, Object> cache;

    /*
     * Create the cache on demand
     */
    public void initialize() {

        // Output expiry debug messages here if required
        var listener = (CacheEntryExpiredListener<String, Object>) (cache, cacheEntry) -> {
        };

        // Create the cache with a default token expiry time
        this.cache = new Cache2kBuilder<String, Object>() {}
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

        // TODO: Avoid exceeding the default cache time as for .Net

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

        // Get the token hash
        var tokenHash = DigestUtils.sha256Hex(accessToken);

        // Return the cached claims if they exist
        var data = cache.get(tokenHash);
        if(data != null) {
            return (TClaims)data;
        }

        // Indicate not found amd that a claims lookup is needed for the new token
        return null;
    }
}
