package com.mycompany.sample.plumbing.utilities;

import org.springframework.security.core.context.SecurityContextHolder;
import com.mycompany.sample.plumbing.claims.CoreApiClaims;

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

        System.out.println("*** GETTING API CLAIMS");
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {

            var principal = authentication.getPrincipal();
            try {
                System.out.println("*** FOUND API CLAIMS");
                return runtimeClass.cast(principal);
            } catch (ClassCastException e) {
                System.out.println("*** NOT FOUND API CLAIMS 1");
                return null;
            }
        }

        System.out.println("*** NOT FOUND API CLAIMS 2");
        return null;
    }
}
