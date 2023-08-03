package com.mycompany.sample.plumbing.claims;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.mycompany.sample.plumbing.dependencies.CustomRequestScope;

/*
 * A helper class to make claims objects injectable
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class ClaimsInjector {

    /*
     * Inject token claims
     */
    @Bean
    @Scope(value = CustomRequestScope.NAME)
    public BaseClaims getTokenClaims() {

        var claims = this.getClaimsPrincipal();
        if (claims != null) {
            return claims.getToken();
        }

        return null;
    }

    /*
     * Inject custom claims
     */
    @Bean
    @Scope(value = CustomRequestScope.NAME)
    public CustomClaims getCustomClaims() {

        var claims = this.getClaimsPrincipal();
        if (claims != null) {
            return claims.getCustom();
        }

        return null;
    }

    /*
     * Get claims from the security context
     */
    private ClaimsPrincipal getClaimsPrincipal() {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return (ClaimsPrincipal) authentication.getPrincipal();
        }

        return null;
    }
}
