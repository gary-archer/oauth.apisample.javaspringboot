package com.mycompany.sample.plumbing.claims;

import com.mycompany.sample.plumbing.dependencies.CustomRequestScope;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/*
 * A helper class to make claims objects injectable
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class ClaimsInjector {

    /*
     * Get claims from the security context
     */
    @Bean
    @Scope(value = CustomRequestScope.NAME)
    public ClaimsPrincipal getClaimsPrincipal() {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return (ClaimsPrincipal) authentication.getPrincipal();
        }

        return null;
    }
}
