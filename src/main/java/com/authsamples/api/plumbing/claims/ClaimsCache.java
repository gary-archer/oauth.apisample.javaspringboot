package com.authsamples.api.plumbing.claims;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.cache2k.event.CacheEntryExpiredListener;
import org.slf4j.Logger;
import com.authsamples.api.plumbing.logging.LoggerFactory;

/*
 * A singleton memory cache for extra authorization values
 */
public class ClaimsCache {

    private final Cache<String, ExtraClaims> cache;
    private final int timeToLiveMinutes;
    private final Logger debugLogger;

    public ClaimsCache(final int timeToLiveMinutes, final LoggerFactory loggerFactory) {

        this.timeToLiveMinutes = timeToLiveMinutes;
        this.debugLogger = loggerFactory.getDebugLogger(ClaimsCache.class);

        // Output expiry debug messages here if required
        CacheEntryExpiredListener<String, ExtraClaims> listener = (cache, cacheEntry) -> {
            var message = String.format(
                    "Expired item has been removed from the cache (hash: %s)",
                    cacheEntry.getKey());

            if (this.debugLogger != null) {
                this.debugLogger.debug(message);
            }
        };

        // Create the cache with a default entry expiry time
        this.cache = new Cache2kBuilder<String, ExtraClaims>() {
        }
                .name("extraClaims")
                .expireAfterWrite(timeToLiveMinutes, TimeUnit.MINUTES)
                .addListener(listener)
                .build();
    }

    /*
     * Add an item to the cache and do not exceed the token's expiry or the configured time to live
     */
    public void setItem(final String accessTokenHash, final ExtraClaims claims, final int expiry) {

        var epochSeconds = Instant.now().getEpochSecond();
        var secondsToCache = expiry - epochSeconds;
        if (secondsToCache > 0) {

            var secondsMultiplier = 60;
            var maxExpirySeconds =
                    Instant.now().getEpochSecond() + (long) this.timeToLiveMinutes * secondsMultiplier;
            if (secondsToCache > maxExpirySeconds) {
                secondsToCache = maxExpirySeconds;
            }

            if (this.debugLogger != null) {
                this.debugLogger.debug(String.format(
                        "Adding item to cache for %d seconds (hash: %s)",
                        secondsToCache,
                        accessTokenHash));
            }

            var futureExpiryMilliseconds = (epochSeconds + secondsToCache) * 1000;
            cache.invoke(accessTokenHash, e -> e.setValue(claims).setExpiryTime(futureExpiryMilliseconds));
        }
    }

    /*
     * Get an item from the cache for this token's hash, or return null if not found
     */
    public ExtraClaims getItem(final String accessTokenHash) {

        var claims = cache.get(accessTokenHash);
        if (claims == null) {

            if (this.debugLogger != null) {
                return null;
            }
        }

        if (this.debugLogger != null) {
            this.debugLogger.debug(
                    String.format("Found existing item in cache (hash: %s)", accessTokenHash));
        }

        return claims;
    }
}
