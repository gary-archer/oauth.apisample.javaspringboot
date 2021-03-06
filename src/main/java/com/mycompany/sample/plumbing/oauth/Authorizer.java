package com.mycompany.sample.plumbing.oauth;

import javax.servlet.http.HttpServletRequest;
import com.mycompany.sample.plumbing.claims.ApiClaims;

/*
 * An authorizer abstraction that could be used for both Entry Point APIs and Microservices
 */
public interface Authorizer {

    ApiClaims execute(HttpServletRequest request);
}
