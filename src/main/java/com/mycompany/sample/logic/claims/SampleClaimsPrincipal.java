package com.mycompany.sample.logic.claims;

import org.jose4j.jwt.JwtClaims;
import com.mycompany.sample.plumbing.claims.ClaimsPrincipal;
import com.mycompany.sample.plumbing.claims.ClaimsReader;
import com.mycompany.sample.plumbing.claims.ExtraClaims;

/*
 * Manages claims that should be issued to the access token, to ensure that it is locked down
 * When the authorization server does not support this, look up such values from extra claims
 */
public final class SampleClaimsPrincipal extends ClaimsPrincipal {

    public SampleClaimsPrincipal(final JwtClaims jwtClaims, final ExtraClaims extraClaims) {
        super(jwtClaims, extraClaims);
    }

    public String getManagerId() {

        if (this.getJwtClaims().hasClaim(CustomClaimNames.ManagerId)) {
           return ClaimsReader.getStringClaim(this.getJwtClaims(), CustomClaimNames.ManagerId);
        } else {
            return ((SampleExtraClaims) getExtraClaims()).getManagerId();
        }
    }

    public String getRole() {

        if (this.getJwtClaims().hasClaim(CustomClaimNames.Role)) {
            return ClaimsReader.getStringClaim(this.getJwtClaims(), CustomClaimNames.Role);
        } else {
            return ((SampleExtraClaims) getExtraClaims()).getRole();
        }
    }
}
