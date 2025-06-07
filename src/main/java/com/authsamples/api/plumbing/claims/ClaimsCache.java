package com.authsamples.api.plumbing.claims;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.cache2k.event.CacheEntryExpiredListener;
import org.slf4j.Logger;
import com.authsamples.api.plumbing.logging.LoggerFactory;

/*
 * A singleton in memory claims cache for our API
 */
public class ClaimsCache {

    private final Cache<String, ExtraClaims> cache;
    private final int timeToLiveMinutes;
    private final Logger debugLogger;

    public ClaimsCache(
            final int timeToLiveMinutes,
            final LoggerFactory loggerFactory) {

        this.timeToLiveMinutes = timeToLiveMinutes;
        this.debugLogger = loggerFactory.getDevelopmentLogger(ClaimsCache.class);

        // Output expiry debug messages here if required
        CacheEntryExpiredListener<String, ExtraClaims> listener = (cache, cacheEntry) -> {
            var message = String.format(
                    "Expired entry has been removed from the cache (hash: %s)",
                    cacheEntry.getKey());
            this.debugLogger.debug(message);
        };

        // Create the cache with a default token expiry time
        this.cache = new Cache2kBuilder<String, ExtraClaims>() {
        }
                .name("claims")
                .expireAfterWrite(timeToLiveMinutes, TimeUnit.MINUTES)
                .addListener(listener)
                .build();
    }

    /*
     * Add claims to the cache until the token's time to live
     */
    public void setExtraUserClaims(final String accessTokenHash, final ExtraClaims claims, final int expiry) {

        // Use the exp field to work out the token expiry time
        var epochSeconds = Instant.now().getEpochSecond();
        var secondsToCache = expiry - epochSeconds;
        if (secondsToCache > 0) {

            // Do not exceed the maximum time we configured
            final var secondsMultiplier = 60;
            var maxExpirySeconds =
                    Instant.now().getEpochSecond() + (long) this.timeToLiveMinutes * secondsMultiplier;
            if (secondsToCache > maxExpirySeconds) {
                secondsToCache = maxExpirySeconds;
            }

            // Output debug info
            this.debugLogger.debug(String.format(
                    "Adding entry to claims cache for %d seconds (hash: %s)",
                    secondsToCache,
                    accessTokenHash));

            // Do the write
            final var futureExpiryMilliseconds = (epochSeconds + secondsToCache) * 1000;
            cache.invoke(accessTokenHash, e -> e.setValue(claims).setExpiryTime(futureExpiryMilliseconds));
        }
    }

    /*
     * Get claims from the cache for this token's hash, or return null if not found
     * Almost simultaneous requests from the same user could return null for the same token
     */
    public ExtraClaims getExtraUserClaims(final String accessTokenHash) {

        // Return null if there are no cached claims
        var claims = cache.get(accessTokenHash);
        if (claims == null) {
            this.debugLogger.debug(
                    String.format("New entry will be added to claims cache (hash: %s)", accessTokenHash));
            return null;
        }

        // Output debug info
        this.debugLogger.debug(
                String.format("Found existing entry in claims cache (hash: %s)", accessTokenHash));

        // Return the result
        return claims;
    }
}
