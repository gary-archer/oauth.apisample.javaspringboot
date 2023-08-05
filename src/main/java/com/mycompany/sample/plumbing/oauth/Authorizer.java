package com.mycompany.sample.plumbing.oauth;

import jakarta.servlet.http.HttpServletRequest;
import com.mycompany.sample.plumbing.claims.ClaimsPrincipal;

/*
 * An authorizer abstraction to validate a JWT access token and return claims
 */
public interface Authorizer {

    ClaimsPrincipal execute(HttpServletRequest request);
}
