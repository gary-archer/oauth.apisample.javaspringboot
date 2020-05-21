package com.mycompany.sample.plumbing.claims;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.digest.DigestUtils;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.cache2k.event.CacheEntryExpiredListener;
import org.slf4j.Logger;

import com.mycompany.sample.plumbing.configuration.OAuthConfiguration;
import com.mycompany.sample.plumbing.logging.LoggerFactory;

/*
 * A singleton in memory claims cache for our API
 */
public final class ClaimsCache<TClaims extends CoreApiClaims> {

    private final OAuthConfiguration configuration;
    private final Logger debugLogger;

    public ClaimsCache(final OAuthConfiguration configuration, final LoggerFactory loggerFactory) {
        this.configuration = configuration;
        this.debugLogger = loggerFactory.getDevelopmentLogger(ClaimsCache.class);
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
        CacheEntryExpiredListener<String, Object> listener = (cache, cacheEntry) -> {
            var message = String.format(
                    "Expired token has been removed from the cache (hash: %s)",
                    cacheEntry.getKey());
            this.debugLogger.debug(message);
        };

        // Create the cache with a default token expiry time
        this.cache = new Cache2kBuilder<String, Object>() {
        }
            .name("claims")
            .expireAfterWrite(this.configuration.getMaxClaimsCacheMinutes(), TimeUnit.MINUTES)
            .addListener(listener)
            .build();
    }



    /*
     * Get claims from the cache for this token's hash, or return null if not found
     * Almost simultaneous requests from the same user could return null for the same token
     */
    public TClaims getClaimsForToken(final String accessToken) {

        // Get the token hash
        var tokenHash = DigestUtils.sha256Hex(accessToken);

        // Return the cached claims if they exist
        var data = cache.get(tokenHash);
        if (data == null) {
            this.debugLogger.debug(String.format("New token will be added to claims cache (hash: %s)", tokenHash));
            return null;
        }

        // Otherwise return cached claims
        this.debugLogger.debug(String.format("Found existing token in claims cache (hash: %s)", tokenHash));
        return (TClaims) data;
    }

    /*
     * Add claims to the cache until the token's time to live
     */
    public void addClaimsForToken(final String accessToken, final long expiry, final TClaims claims) {

        // Use the exp field returned from introspection to work out the token expiry time
        var epochSeconds = Instant.now().getEpochSecond();
        var secondsToCache = expiry - epochSeconds;
        if (secondsToCache > 0) {

            // Get the hash and output debug info
            var tokenHash = DigestUtils.sha256Hex(accessToken);
            this.debugLogger.debug(String.format(
                "Token to be cached will expire in %d seconds (hash: %s)",
                secondsToCache,
                tokenHash));

            // Do not exceed the maximum time we configured
            final var seconds = 60;
            var maxExpirySeconds =
                    Instant.now().getEpochSecond() + this.configuration.getMaxClaimsCacheMinutes() * seconds;
            if (secondsToCache > maxExpirySeconds) {
                secondsToCache = maxExpirySeconds;
            }

            // Add to the cache, which requires an absolute time in the future in milliseconds
            this.debugLogger.debug(String.format(
                "Adding token to claims cache for %d seconds (hash: %s)",
                secondsToCache,
                tokenHash));
            final var futureExpiryMilliseconds = (epochSeconds + secondsToCache) * 1000;
            cache.invoke(tokenHash, e -> e.setValue(claims).setExpiryTime(futureExpiryMilliseconds));
        }
    }
}
