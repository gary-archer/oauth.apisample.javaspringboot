package com.mycompany.sample.logic.claims;

import org.jose4j.jwt.JwtClaims;
import org.springframework.beans.factory.BeanFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.mycompany.sample.logic.repositories.UserRepository;
import com.mycompany.sample.plumbing.claims.ClaimsPrincipal;
import com.mycompany.sample.plumbing.claims.ClaimsReader;
import com.mycompany.sample.plumbing.claims.ExtraClaims;
import com.mycompany.sample.plumbing.claims.ExtraClaimsProvider;

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

        // First, see which claims are included in access tokens
        if (jwtClaims.hasClaim(CustomClaimNames.ManagerId)) {

            // The best model is to receive a useful user identity in access tokens, along with the user role
            // This ensures a locked down token and also simpler code
            var managerId = ClaimsReader.getStringClaim(jwtClaims, CustomClaimNames.ManagerId);
            return userRepository.getClaimsForManagerId(managerId);

        } else {

            // With AWS Cognito, there is a lack of support for custom claims in access tokens at the time of writing
            // So the API has to map the subject to its own user identity and look up all custom claims
            var subject = ClaimsReader.getStringClaim(jwtClaims, "sub");
            return userRepository.getClaimsForSubject(subject);
        }
    }

    /*
     * Create a claims principal that manages lookups across both token claims and extra claims
     */
    @Override
    public ClaimsPrincipal createClaimsPrincipal(final JwtClaims jwtClaims, final ExtraClaims extraClaims) {
        return new SampleClaimsPrincipal(jwtClaims, extraClaims);
    }

    /*
     * Deserialize extra claims after they have been read from the cache
     */
    @Override
    public ExtraClaims deserializeFromCache(final JsonNode claimsNode) {
        return SampleExtraClaims.importData(claimsNode);
    }
}
