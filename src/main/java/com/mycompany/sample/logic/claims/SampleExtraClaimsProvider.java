package com.mycompany.sample.logic.claims;

import org.jose4j.jwt.JwtClaims;
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

    private final UserRepository userRepository;

    public SampleExtraClaimsProvider(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /*
     * Get additional claims from the API's own database
     */
    @Override
    public ExtraClaims lookupExtraClaims(final JwtClaims jwtClaims) {

        // First, see which claims are included in access tokens
        if (jwtClaims.hasClaim("manager_id")) {

            // The best model is to receive a useful user identity in access tokens, along with the user role
            var managerId = ClaimsReader.getStringClaim(jwtClaims, "manager_id");
            return this.userRepository.getClaimsForManagerId(managerId);

        } else {

            // For AWS Cognito, the API has to map the subject to its own user identity and look up all custom claims
            var subject = ClaimsReader.getStringClaim(jwtClaims, "sub");
            return this.userRepository.getClaimsForSubject(subject);
        }
    }

    /*
     * Return a claims principal with some claims lookup workarounds
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
