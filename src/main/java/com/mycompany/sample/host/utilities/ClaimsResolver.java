package com.mycompany.sample.host.utilities;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.mycompany.sample.host.claims.SampleApiClaims;
import com.mycompany.sample.plumbing.dependencies.CustomRequestScope;

/*
 * An injectable object to get claims in a type safe manner
 */
@Component
@Scope(value = CustomRequestScope.NAME)
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class ClaimsResolver {

    /*
     * Get the claims object when request
     */
    @Bean
    @Scope(value = CustomRequestScope.NAME)
    public SampleApiClaims getClaims() {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return (SampleApiClaims) authentication.getPrincipal();
        }

        return null;
    }
}
