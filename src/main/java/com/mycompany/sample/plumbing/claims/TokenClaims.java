package com.mycompany.sample.plumbing.claims;

import org.jose4j.jwt.JwtClaims;
import lombok.Getter;

/*
 * Represents claims designed to be included in the access token, to lock down its privileges
 * When there are technical limitations that prevent this, a derived instance can store such values
 */
public class TokenClaims {

    @Getter
    private final JwtClaims jwtClaims;

    public TokenClaims(final JwtClaims jwtClaims) {
        this.jwtClaims = jwtClaims;
    }

    @SuppressWarnings(value = "checkstyle:DesignForExtension")
    public String getSubject() {
        return ClaimsReader.getStringClaim(this.jwtClaims, "sub");
    }
}
