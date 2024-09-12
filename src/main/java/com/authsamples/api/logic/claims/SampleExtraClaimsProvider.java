package com.authsamples.api.logic.claims;

import org.jose4j.jwt.JwtClaims;
import org.springframework.beans.factory.BeanFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.authsamples.api.logic.repositories.UserRepository;
import com.authsamples.api.plumbing.claims.ClaimsPrincipal;
import com.authsamples.api.plumbing.claims.ClaimsReader;
import com.authsamples.api.plumbing.claims.ExtraClaims;
import com.authsamples.api.plumbing.claims.ExtraClaimsProvider;

/*
 * Add extra claims that you cannot, or do not want to, manage in the authorization server
 */
public final class SampleExtraClaimsProvider extends ExtraClaimsProvider {

    private final BeanFactory container;

    public SampleExtraClaimsProvider(final BeanFactory container) {
        this.container = container;
    }

    /*
     * Get additional claims from the API's own database
     */
    @Override
    public ExtraClaims lookupExtraClaims(final JwtClaims jwtClaims) {

        // Get an object to look up user information
        var userRepository = this.container.getBean(UserRepository.class);

        // The manager ID is a business user identity from which other claims can be looked up
        var managerId = ClaimsReader.getStringClaim(jwtClaims, CustomClaimNames.ManagerId);
        return userRepository.getClaimsForManagerId(managerId);
    }

    /*
     * Create a claims principal containing all claims
     */
    @Override
    public ClaimsPrincipal createClaimsPrincipal(final JwtClaims jwtClaims, final ExtraClaims extraClaims) {
        return new ClaimsPrincipal(jwtClaims, extraClaims);
    }

    /*
     * Deserialize extra claims after they have been read from the cache
     */
    @Override
    public ExtraClaims deserializeFromCache(final JsonNode claimsNode) {
        return SampleExtraClaims.importData(claimsNode);
    }
}
