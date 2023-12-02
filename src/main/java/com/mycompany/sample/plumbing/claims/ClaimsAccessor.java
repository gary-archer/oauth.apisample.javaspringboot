package com.mycompany.sample.plumbing.claims;

import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.mycompany.sample.plumbing.dependencies.CustomRequestScope;

/*
 * A helper class to make claims objects injectable
 */
@Component
@Scope(value = CustomRequestScope.NAME)
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class ClaimsAccessor {

    /*
     * Return claims from the security context
     * At startup, this is configured with MODE_INHERITABLETHREADLOCAL
     * It is therefore safely accessible across multiple async threads during the request lifecycle
     */
    public ClaimsPrincipal getMyPrincipal() {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            var principal = (ClaimsPrincipal) authentication.getPrincipal();
            System.out.println("*** GetMyPrincipal: Found claims: " + principal.getExtraClaims().exportData().toPrettyString());
            return principal;
        } else {
            System.out.println("*** GetMyPrincipal: Not found claims");
        }

        return null;
    }
}
