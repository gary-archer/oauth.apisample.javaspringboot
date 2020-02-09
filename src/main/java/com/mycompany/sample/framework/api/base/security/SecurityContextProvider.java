package com.mycompany.sample.framework.api.base.security;

import org.springframework.security.core.context.SecurityContextHolder;

/*
 * A utility class to inject the results of OAuth processing into business logic
 */
public final class SecurityContextProvider {

    private SecurityContextProvider() {
    }

    /*
     * Return the API claims for the current request
     * We avoid returning a Bean, since that creates the object before the security context is ready
     */
    public static <T extends CoreApiClaims> T getClaims(final Class<T> runtimeClass) {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {

            var principal = authentication.getPrincipal();
            try {
                return runtimeClass.cast(principal);
            } catch (ClassCastException e) {
                return null;
            }
        }

        return null;
    }
}
