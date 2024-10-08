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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/*
 * A singleton in memory claims cache for our API
 */
@SuppressWarnings(value = "checkstyle:DesignForExtension")
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

            // Serialize the data
            var claimsJson = claims.exportData().toString();

            // Output debug info
            this.debugLogger.debug(String.format(
                    "Adding token to claims cache for %d seconds (hash: %s)",
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
    public ExtraClaims getExtraUserClaims(final String accessTokenHash) {

        // Return null if there are no cached claims
        var claimsText = cache.get(accessTokenHash);
        if (!StringUtils.hasLength(claimsText)) {
            this.debugLogger.debug(
                    String.format("New token will be added to claims cache (hash: %s)", accessTokenHash));
            return null;
        }

        try {

            // Deserialize the data
            var mapper = new ObjectMapper();
            var data = mapper.readValue(claimsText, ObjectNode.class);
            var claims = this.extraClaimsProvider.deserializeFromCache(data);

            // Output debug info
            this.debugLogger.debug(
                    String.format("Found existing token in claims cache (hash: %s)", accessTokenHash));

            // Return the result
            return claims;

        } catch (Throwable ex) {

            // Report error details
            throw ErrorFactory.createServerError(
                    ErrorCodes.JSON_PARSE_ERROR,
                    "Problem encountered parsing JSON claims data",
                    ex);
        }
    }
}
