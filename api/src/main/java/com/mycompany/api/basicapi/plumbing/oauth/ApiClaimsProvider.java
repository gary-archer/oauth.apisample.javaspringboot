package com.mycompany.api.basicapi.plumbing.oauth;

import com.mycompany.api.basicapi.entities.ApiClaims;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/*
 * Rather than access the security context in our logic we'll inject a class that could be abstracted behind an interface
 * This can be safely used as a singleton because is used a static class and stores no state
 */
@Component
@RequestScope
public class ApiClaimsProvider {

    /*
     * Return the API claims set by the security handler
     */
    public ApiClaims getApiClaims() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null) {
            Object principal = authentication.getPrincipal();
            if(principal instanceof ApiClaims) {
                return (ApiClaims)principal;
            }
        }

        throw new RuntimeException("Unable to retrieve API claims from the security context");
    }
}
