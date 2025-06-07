package com.authsamples.api.plumbing.claims;

import org.jose4j.jwt.JwtClaims;

/*
 * An interface for the claims principal
 */
public interface ClaimsPrincipal<T> {

    /*
     * Return token claims
     */
    JwtClaims getJwtClaims();

    /*
     * Return extra claims
     */
    T getExtraClaims();

    /*
     * Return the subject claim
     */
    String getSubject();
}
