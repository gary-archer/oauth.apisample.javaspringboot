package com.mycompany.sample.plumbing.claims;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.cache2k.event.CacheEntryExpiredListener;
import org.slf4j.Logger;
import org.springframework.util.StringUtils;
import com.mycompany.sample.plumbing.logging.LoggerFactory;

/*
 * A singleton in memory claims cache for our API
 */
public final class ClaimsCache {

    private Cache<String, String> cache;
    private int timeToLiveMinutes;
    private final ClaimsProvider serializer;
    private final Logger debugLogger;

    public ClaimsCache(
            final int timeToLiveMinutes,
            final ClaimsProvider serializer,
            final LoggerFactory loggerFactory) {

        this.timeToLiveMinutes = timeToLiveMinutes;
        this.serializer = serializer;
        this.debugLogger = loggerFactory.getDevelopmentLogger(ClaimsCache.class);

        // Output expiry debug messages here if required
        CacheEntryExpiredListener<String, String> listener = (cache, cacheEntry) -> {
            var message = String.format(
                    "Expired token has been removed from the cache (hash: %s)",
                    cacheEntry.getKey());
            this.debugLogger.debug(message);
        };

        // Create the cache with a default token expiry time
        this.cache = new Cache2kBuilder<String, String>() {
        }
                .name("claims")
                .expireAfterWrite(timeToLiveMinutes, TimeUnit.MINUTES)
                .addListener(listener)
                .build();
    }

    /*
     * Get claims from the cache for this token's hash, or return null if not found
     * Almost simultaneous requests from the same user could return null for the same token
     */
    public ApiClaims getClaimsForToken(final String accessTokenHash) {

        // Return null if there are no cached claims
        var claimsText = cache.get(accessTokenHash);
        if (!StringUtils.hasLength(claimsText)) {
            this.debugLogger.debug(
                    String.format("New token will be added to claims cache (hash: %s)", accessTokenHash));
            return null;
        }

        // Otherwise return cached claims
        this.debugLogger.debug(
                String.format("Found existing token in claims cache (hash: %s)", accessTokenHash));
        return this.serializer.deserializeFromCache(claimsText);
    }

    /*
     * Add claims to the cache until the token's time to live
     */
    public void addClaimsForToken(final String accessTokenHash, final ApiClaims claims) {

        // Use the exp field returned from introspection to work out the token expiry time
        var epochSeconds = Instant.now().getEpochSecond();
        var secondsToCache = claims.getToken().getExpiry() - epochSeconds;
        if (secondsToCache > 0) {

            // Get the hash and output debug info
            this.debugLogger.debug(String.format(
                    "Token to be cached will expire in %d seconds (hash: %s)",
                    secondsToCache,
                    accessTokenHash));

            // Do not exceed the maximum time we configured
            final var secondsMultiplier = 60;
            var maxExpirySeconds =
                    Instant.now().getEpochSecond() + (long) this.timeToLiveMinutes * secondsMultiplier;
            if (secondsToCache > maxExpirySeconds) {
                secondsToCache = maxExpirySeconds;
            }

            // Add to the cache, which requires an absolute time in the future in milliseconds
            this.debugLogger.debug(String.format(
                    "Adding token to claims cache for %d seconds (hash: %s)",
                    secondsToCache,
                    accessTokenHash));

            final var futureExpiryMilliseconds = (epochSeconds + secondsToCache) * 1000;
            var claimsText = this.serializer.serializeToCache(claims);
            cache.invoke(accessTokenHash, e -> e.setValue(claimsText).setExpiryTime(futureExpiryMilliseconds));
        }
    }
}
