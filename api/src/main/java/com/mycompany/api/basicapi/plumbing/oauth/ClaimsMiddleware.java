package com.mycompany.api.basicapi.plumbing.oauth;

import com.mycompany.api.basicapi.logic.AuthorizationRulesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * The entry point for the processing to validate tokens and return claims
 * Our approach provides extensible claims to our API and enables high performance
 * It also takes close control of error responses to our SPA
 */
public class ClaimsMiddleware {

    /*
     * Injected dependencies
     */
    private final ClaimsCache cache;
    private final Authenticator authenticator;
    private final AuthorizationRulesRepository rulesRepository;
    private final Logger logger;

    /*
     * Receive dependencies
     */
    public ClaimsMiddleware(ClaimsCache cache, Authenticator authenticator, AuthorizationRulesRepository rulesRepository) {
        this.cache = cache;
        this.authenticator = authenticator;
        this.rulesRepository = rulesRepository;
        this.logger = LoggerFactory.getLogger(ClaimsMiddleware.class);
    }

    /*
     * The entry point function
     */
    public boolean authorizeRequestAndSetClaims(String accessToken, CoreApiClaims claims) {

        // First report missing tokens
        if (accessToken == null) {
            return false;
        }

        // Bypass validation and use cached results if they exist
        var cacheSuccess = this.cache.getClaimsForToken(accessToken, claims);
        if (cacheSuccess) {
            this.logger.info("Claims Middleware: Existing claims returned from cache");
            return true;
        }

        // Otherwise start by introspecting the token
        var result = this.authenticator.validateTokenAndSetClaims(accessToken, claims);
        var tokenSuccess = result.getValue0();
        var expiry = result.getValue1();
        if (!tokenSuccess) {
            this.logger.info("Claims Middleware: Invalid or expired access token");
            return false;
        }

        // Next add central user info to the user's claims
        var userInfoSuccess = this.authenticator.setCentralUserInfoClaims(accessToken, claims);
        if(!userInfoSuccess) {
            this.logger.info("Claims Middleware: Expired access token used for user info lookup");
            return false;
        }

        // Look up any product specific custom claims if required
        if(this.rulesRepository != null) {
            this.rulesRepository.addCustomClaims(accessToken, claims);
        }

        // Cache the claims against the token hash until the token's expiry time
        // The next time the API is called, all of the above results can be quickly looked up
        cache.addClaimsForToken(accessToken, expiry.longValue(), claims);
        this.logger.info("Claims Middleware: Claims lookup completed successfully");
        return true;
    }
}
