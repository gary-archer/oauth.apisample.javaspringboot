package com.authsamples.api.middleware.claims;

import org.jose4j.jwt.JwtClaims;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.authsamples.api.logic.repositories.UserRepository;
import com.authsamples.api.middleware.claims.ClaimsReader;
import com.authsamples.api.middleware.claims.CustomClaimNames;
import com.authsamples.api.middleware.claims.ExtraClaims;
import com.authsamples.api.middleware.claims.ExtraClaimsProvider;

/*
 * Add extra authorization values that you cannot, or do not want to, manage in the authorization server
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public final class ExtraClaimsProvider {

    private final UserRepository userRepository;

    public ExtraClaimsProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /*
     * Get extra values from the API's own data
     */
    public ExtraClaims lookupExtraClaims(JwtClaims jwtClaims) {

        var managerId = ClaimsReader.getStringClaim(jwtClaims, CustomClaimNames.ManagerId);
        return this.userRepository.getUserInfoForManagerId(managerId);
    }
}
