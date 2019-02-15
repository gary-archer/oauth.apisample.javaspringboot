package com.mycompany.api.basicapi.framework.oauth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.function.Supplier;

/*
 * The entry point for the processing to validate tokens and return claims
 * Our approach provides extensible claims to our API and enables high performance
 * It also takes close control of error responses to our SPA
 */
public class ClaimsMiddleware<TClaims extends CoreApiClaims> {

    /*
     * Injected dependencies
     */
    private final ClaimsCache<TClaims> cache;
    private final Authenticator authenticator;
    private final CustomClaimsProvider<TClaims> customClaimsProvider;
    private final Logger logger;

    /*
     * Receive dependencies
     */
    public ClaimsMiddleware(ClaimsCache<TClaims> cache, Authenticator authenticator, CustomClaimsProvider<TClaims> customClaimsProvider) {
        this.cache = cache;
        this.authenticator = authenticator;
        this.customClaimsProvider = customClaimsProvider;
        this.logger = LoggerFactory.getLogger(ClaimsMiddleware.class);
    }

    /*
     * The entry point function
     */
    public TClaims authorizeRequestAndGetClaims(String accessToken, Supplier<TClaims> claimsSupplier) {

        // First report missing tokens
        if (accessToken == null) {
            return null;
        }

        // Bypass validation and use cached results if they exist
        var cachedClaims = this.cache.getClaimsForToken(accessToken);
        if (cachedClaims != null) {
            this.logger.info("Claims Middleware: Existing claims returned from cache");
            return cachedClaims;
        }

        // Otherwise create new claims which we will populate
        var claims = claimsSupplier.get();

        // Otherwise start by introspecting the token
        var result = this.authenticator.validateTokenAndSetClaims(accessToken, claims);
        var tokenSuccess = result.getValue0();
        var expiry = result.getValue1();
        if (!tokenSuccess) {
            this.logger.info("Claims Middleware: Invalid or expired access token");
            return null;
        }

        // Next add central user info to the user's claims
        var userInfoSuccess = this.authenticator.setCentralUserInfoClaims(accessToken, claims);
        if(!userInfoSuccess) {
            this.logger.info("Claims Middleware: Expired access token used for user info lookup");
            return null;
        }

        // Add any custom product specific custom claims if required
        this.customClaimsProvider.addCustomClaims(accessToken, claims);

        // Cache the claims against the token hash until the token's expiry time
        // The next time the API is called, all of the above results can be quickly looked up
        cache.addClaimsForToken(accessToken, expiry.longValue(), claims);
        this.logger.info("Claims Middleware: Claims lookup completed successfully");
        return claims;
    }
}
