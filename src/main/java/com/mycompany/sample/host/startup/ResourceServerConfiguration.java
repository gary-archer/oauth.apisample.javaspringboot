package com.mycompany.sample.host.startup;

import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.stereotype.Component;

/*
 * A class to manage OAuth specific API configuration
 */
@Component
public class ResourceServerConfiguration implements OpaqueTokenIntrospector {

    @Override
    public OAuth2AuthenticatedPrincipal introspect(String token) {
        return null;
    }
}