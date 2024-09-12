package com.authsamples.api.plumbing.oauth;

import jakarta.servlet.http.HttpServletRequest;
import com.authsamples.api.plumbing.claims.ClaimsPrincipal;

/*
 * An abstraction for doing the OAuth work to validate a JWT access token and return claims
 */
public interface OAuthFilter {
    ClaimsPrincipal execute(HttpServletRequest request);
}
