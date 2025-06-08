package com.authsamples.api.logic.claims;

import org.jose4j.jwt.JwtClaims;
import org.springframework.beans.factory.BeanFactory;
import com.authsamples.api.logic.repositories.UserRepository;
import com.authsamples.api.plumbing.claims.ClaimsReader;
import com.authsamples.api.plumbing.claims.CustomClaimNames;
import com.authsamples.api.plumbing.claims.ExtraValues;
import com.authsamples.api.plumbing.claims.ExtraValuesProvider;

/*
 * Add extra authorization values that you cannot, or do not want to, manage in the authorization server
 */
public final class ExtraValuesProviderImpl implements ExtraValuesProvider {

    private final BeanFactory container;

    public ExtraValuesProviderImpl(final BeanFactory container) {
        this.container = container;
    }

    /*
     * Get extra values from the API's own data
     */
    public ExtraValues lookupExtraValues(final JwtClaims jwtClaims) {

        // Get an object to look up user information
        var userRepository = this.container.getBean(UserRepository.class);

        // Look up values using the manager ID, a business user identity
        var managerId = ClaimsReader.getStringClaim(jwtClaims, CustomClaimNames.ManagerId);
        return userRepository.getUserInfoForManagerId(managerId);
    }
}
