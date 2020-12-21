package com.mycompany.sample.plumbing.utilities;

import org.springframework.security.core.context.SecurityContextHolder;
import com.mycompany.sample.plumbing.claims.CoreApiClaims;

/*
 * A utility class to inject the results of OAuth processing into business logic
 */
@SuppressWarnings("PMD.ClassNamingConventions")
public final class SecurityContextProvider {

    private SecurityContextProvider() {
    }

    /*
     * Return the API claims for the current request
     */
    public static <T extends CoreApiClaims> T getClaims(final Class<T> runtimeClass) {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {

            var principal = authentication.getPrincipal();
            try {
                return runtimeClass.cast(principal);

            } catch (ClassCastException e) {
                throw new IllegalStateException("Problem encountered casting the claims principal", e);
            }
        }

        return null;
    }
}
