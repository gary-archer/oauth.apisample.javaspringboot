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
public class ExtraValuesCache {

    private final Cache<String, ExtraValues> cache;
    private final int timeToLiveMinutes;
    private final Logger debugLogger;

    public ExtraValuesCache(final int timeToLiveMinutes, final LoggerFactory loggerFactory) {

        this.timeToLiveMinutes = timeToLiveMinutes;
        this.debugLogger = loggerFactory.getDevelopmentLogger(ExtraValuesCache.class);

        // Output expiry debug messages here if required
        CacheEntryExpiredListener<String, ExtraValues> listener = (cache, cacheEntry) -> {
            var message = String.format(
                    "Expired entry has been removed from the cache (hash: %s)",
                    cacheEntry.getKey());
            this.debugLogger.debug(message);
        };

        // Create the cache with a default entry expiry time
        this.cache = new Cache2kBuilder<String, ExtraValues>() {
        }
                .name("claims")
                .expireAfterWrite(timeToLiveMinutes, TimeUnit.MINUTES)
                .addListener(listener)
                .build();
    }

    /*
     * Add an item to the cache and do not exceed the token's expiry or the configured time to live
     */
    public void setItem(final String accessTokenHash, final ExtraValues values, final int expiry) {

        var epochSeconds = Instant.now().getEpochSecond();
        var secondsToCache = expiry - epochSeconds;
        if (secondsToCache > 0) {

            var secondsMultiplier = 60;
            var maxExpirySeconds =
                    Instant.now().getEpochSecond() + (long) this.timeToLiveMinutes * secondsMultiplier;
            if (secondsToCache > maxExpirySeconds) {
                secondsToCache = maxExpirySeconds;
            }

            this.debugLogger.debug(String.format(
                    "Adding entry to claims cache for %d seconds (hash: %s)",
                    secondsToCache,
                    accessTokenHash));

            var futureExpiryMilliseconds = (epochSeconds + secondsToCache) * 1000;
            cache.invoke(accessTokenHash, e -> e.setValue(values).setExpiryTime(futureExpiryMilliseconds));
        }
    }

    /*
     * Get an item from the cache for this token's hash, or return null if not found
     */
    public ExtraValues getItem(final String accessTokenHash) {

        var values = cache.get(accessTokenHash);
        if (values == null) {
            this.debugLogger.debug(
                    String.format("New entry will be added to claims cache (hash: %s)", accessTokenHash));
            return null;
        }

        this.debugLogger.debug(
                String.format("Found existing entry in claims cache (hash: %s)", accessTokenHash));

        return values;
    }
}
