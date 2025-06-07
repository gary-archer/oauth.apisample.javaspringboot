package com.authsamples.api.plumbing.claims;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.cache2k.event.CacheEntryExpiredListener;
import org.slf4j.Logger;
import org.springframework.util.StringUtils;
import com.authsamples.api.plumbing.errors.ErrorCodes;
import com.authsamples.api.plumbing.errors.ErrorFactory;
import com.authsamples.api.plumbing.logging.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/*
 * A singleton in memory claims cache for our API
 */
public class ClaimsCache {

    private final Cache<String, String> cache;
    private final ExtraClaimsProvider extraClaimsProvider;
    private final int timeToLiveMinutes;
    private final Logger debugLogger;

    public ClaimsCache(
            final ExtraClaimsProvider extraClaimsProvider,
            final int timeToLiveMinutes,
            final LoggerFactory loggerFactory) {

        this.extraClaimsProvider = extraClaimsProvider;
        this.timeToLiveMinutes = timeToLiveMinutes;
        this.debugLogger = loggerFactory.getDevelopmentLogger(ClaimsCache.class);

        // Output expiry debug messages here if required
        CacheEntryExpiredListener<String, String> listener = (cache, cacheEntry) -> {
            var message = String.format(
                    "Expired entry has been removed from the cache (hash: %s)",
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
     * Add claims to the cache until the token's time to live
     */
    public void setExtraUserClaims(final String accessTokenHash, final Object claims, final int expiry) {

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

            // Serialize the claims to JSON
            String claimsJson;
            try {

                claimsJson = new ObjectMapper().writeValueAsString(claims);

            } catch (JsonProcessingException ex) {

                throw ErrorFactory.createServerError(
                        ErrorCodes.JSON_SERIALIZE_ERROR,
                        "Unable to serialize extra claims");
            }

            // Output debug info
            this.debugLogger.debug(String.format(
                    "Adding entry to claims cache for %d seconds (hash: %s)",
                    secondsToCache,
                    accessTokenHash));

            // Do the write
            final var futureExpiryMilliseconds = (epochSeconds + secondsToCache) * 1000;
            cache.invoke(accessTokenHash, e -> e.setValue(claimsJson).setExpiryTime(futureExpiryMilliseconds));
        }
    }

    /*
     * Get claims from the cache for this token's hash, or return null if not found
     * Almost simultaneous requests from the same user could return null for the same token
     */
    public Object getExtraUserClaims(final String accessTokenHash) {

        // Return null if there are no cached claims
        var claimsJson = cache.get(accessTokenHash);
        if (!StringUtils.hasLength(claimsJson)) {
            this.debugLogger.debug(
                    String.format("New entry will be added to claims cache (hash: %s)", accessTokenHash));
            return null;
        }

        // Deserialize the data
        var claims = this.extraClaimsProvider.deserializeFromCache(claimsJson);

        // Output debug info
        this.debugLogger.debug(
                String.format("Found existing entry in claims cache (hash: %s)", accessTokenHash));

        // Return the result
        return claims;
    }
}
