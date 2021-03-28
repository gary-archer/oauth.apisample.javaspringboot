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

    private Cache<String, String> _cache;
    private int _timeToLiveMinutes;
    private final CustomClaimsProvider _serializer;
    private final Logger _debugLogger;

    public ClaimsCache(
            final int timeToLiveMinutes,
            final CustomClaimsProvider serializer,
            final LoggerFactory loggerFactory) {

        this._timeToLiveMinutes = timeToLiveMinutes;
        this._serializer = serializer;
        this._debugLogger = loggerFactory.getDevelopmentLogger(ClaimsCache.class);

        // Output expiry debug messages here if required
        CacheEntryExpiredListener<String, String> listener = (cache, cacheEntry) -> {
            var message = String.format(
                    "Expired token has been removed from the cache (hash: %s)",
                    cacheEntry.getKey());
            this._debugLogger.debug(message);
        };

        // Create the cache with a default token expiry time
        this._cache = new Cache2kBuilder<String, String>() {
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
        var claimsText = _cache.get(accessTokenHash);
        if (!StringUtils.hasLength(claimsText)) {
            this._debugLogger.debug(
                    String.format("New token will be added to claims cache (hash: %s)", accessTokenHash));
            return null;
        }

        // Otherwise return cached claims
        this._debugLogger.debug(
                String.format("Found existing token in claims cache (hash: %s)", accessTokenHash));
        return this._serializer.deserializeFromCache(claimsText);
    }

    /*
     * Add claims to the cache until the token's time to live
     */
    public void addClaimsForToken(final String accessTokenHash, final ApiClaims claims) {

        // Use the exp field returned from introspection to work out the token expiry time
        var epochSeconds = Instant.now().getEpochSecond();
        var secondsToCache = claims.get_token().get_expiry() - epochSeconds;
        if (secondsToCache > 0) {

            // Get the hash and output debug info
            this._debugLogger.debug(String.format(
                    "Token to be cached will expire in %d seconds (hash: %s)",
                    secondsToCache,
                    accessTokenHash));

            // Do not exceed the maximum time we configured
            final var secondsMultiplier = 60;
            var maxExpirySeconds =
                    Instant.now().getEpochSecond() + (long) this._timeToLiveMinutes * secondsMultiplier;
            if (secondsToCache > maxExpirySeconds) {
                secondsToCache = maxExpirySeconds;
            }

            // Add to the cache, which requires an absolute time in the future in milliseconds
            this._debugLogger.debug(String.format(
                    "Adding token to claims cache for %d seconds (hash: %s)",
                    secondsToCache,
                    accessTokenHash));

            final var futureExpiryMilliseconds = (epochSeconds + secondsToCache) * 1000;
            var claimsText = this._serializer.serializeToCache(claims);
            _cache.invoke(accessTokenHash, e -> e.setValue(claimsText).setExpiryTime(futureExpiryMilliseconds));
        }
    }
}
