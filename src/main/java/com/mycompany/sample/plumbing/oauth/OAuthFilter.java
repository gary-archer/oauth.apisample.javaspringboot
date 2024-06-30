package com.authsamples.api.plumbing.oauth;

import jakarta.servlet.http.HttpServletRequest;
import com.authsamples.api.plumbing.claims.ClaimsPrincipal;

/*
 * An authorizer abstraction to validate a JWT access token and return claims
 */
public interface OAuthFilter {

    ClaimsPrincipal execute(HttpServletRequest request);
}
