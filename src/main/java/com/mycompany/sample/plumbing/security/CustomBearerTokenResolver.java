package com.mycompany.sample.plumbing.security;

import javax.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;

/*
 * We override this to consolidate our own error handling and logging
 * The goal is to avoid different error paths for missing / invalid tokens
 */
public final class CustomBearerTokenResolver implements BearerTokenResolver {

    /*
     * This forces all API requests to go via the CustomAuthenticationManager
     * We do not use the below dummy value and instead we read the request header ourselves
     */
    @Override
    public String resolve(final HttpServletRequest httpServletRequest) {

        BearerTokenAuthenticationFilter x;
        return "TOKEN";
    }
}
