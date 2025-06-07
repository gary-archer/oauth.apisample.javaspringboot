package com.authsamples.api.plumbing.claims;

import org.jose4j.jwt.JwtClaims;
import org.springframework.beans.factory.BeanFactory;
import com.authsamples.api.logic.repositories.UserRepository;
import com.authsamples.api.plumbing.errors.BaseErrorCodes;
import com.authsamples.api.plumbing.errors.ErrorFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/*
 * Add extra claims that you cannot, or do not want to, manage in the authorization server
 */
public final class ExtraClaimsProvider {

    private final BeanFactory container;

    public ExtraClaimsProvider(final BeanFactory container) {
        this.container = container;
    }

    /*
     * Get extra claims from the API's own data
     */
    public ExtraClaims lookupExtraClaims(final JwtClaims jwtClaims) {

        // Get an object to look up user information
        var userRepository = this.container.getBean(UserRepository.class);

        // The manager ID is a business user identity from which other claims can be looked up
        var managerId = ClaimsReader.getStringClaim(jwtClaims, CustomClaimNames.ManagerId);
        return userRepository.getUserInfoForManagerId(managerId);
    }

    /*
     * Create a custom claims principal
     */
    public ClaimsPrincipal createClaimsPrincipal(final JwtClaims jwtClaims, final ExtraClaims extraClaims) {
        return new ClaimsPrincipal(jwtClaims, extraClaims);
    }

    /*
     * Load extra claims from the cache
     */
    public ExtraClaims deserializeFromCache(final String json) {

        try {

            var mapper = new ObjectMapper();
            return mapper.readValue(json, ExtraClaims.class);

        } catch (JsonProcessingException ex) {

            throw ErrorFactory.createServerError(BaseErrorCodes.JSON_PARSE_ERROR, "Unable to parse extra claims", ex);
        }
    }
}
