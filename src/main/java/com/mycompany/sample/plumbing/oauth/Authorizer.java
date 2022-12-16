package com.mycompany.sample.plumbing.oauth;

import jakarta.servlet.http.HttpServletRequest;
import com.mycompany.sample.plumbing.claims.ClaimsPrincipal;

/*
 * An authorizer abstraction that could be used for both Entry Point APIs and Microservices
 */
public interface Authorizer {

    ClaimsPrincipal execute(HttpServletRequest request);
}
