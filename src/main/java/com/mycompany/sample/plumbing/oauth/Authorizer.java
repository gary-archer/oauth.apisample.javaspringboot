package com.mycompany.sample.plumbing.oauth;

import javax.servlet.http.HttpServletRequest;
import com.mycompany.sample.plumbing.claims.CoreApiClaims;

/*
 * An authorizer abstraction that could be used for both Entry Point APIs and Microservices
 */
public interface Authorizer {

    CoreApiClaims execute(HttpServletRequest request);
}
