package com.authsamples.api.logic.claims;

import org.jose4j.jwt.JwtClaims;
import org.springframework.beans.factory.BeanFactory;
import com.authsamples.api.logic.repositories.UserRepository;
import com.authsamples.api.plumbing.claims.ClaimsReader;
import com.authsamples.api.plumbing.claims.CustomClaimNames;
import com.authsamples.api.plumbing.claims.ExtraClaims;
import com.authsamples.api.plumbing.claims.ExtraClaimsProvider;

/*
 * Add extra authorization values that you cannot, or do not want to, manage in the authorization server
 */
public final class ExtraClaimsProviderImpl implements ExtraClaimsProvider {

    private final BeanFactory container;

    public ExtraClaimsProviderImpl(final BeanFactory container) {
        this.container = container;
    }

    /*
     * Get extra values from the API's own data
     */
    public ExtraClaims lookupExtraClaims(final JwtClaims jwtClaims) {

        // Get an object to look up user information
        var userRepository = this.container.getBean(UserRepository.class);

        // Look up values using the manager ID, a business user identity
        var managerId = ClaimsReader.getStringClaim(jwtClaims, CustomClaimNames.ManagerId);
        return userRepository.getUserInfoForManagerId(managerId);
    }
}
