package com.mycompany.sample.host.utilities;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.mycompany.sample.host.claims.SampleApiClaims;
import com.mycompany.sample.plumbing.dependencies.CustomRequestScope;
import com.mycompany.sample.plumbing.utilities.SecurityContextProvider;

/*
 * A utility to inject the claims from the security context into business logic
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class ClaimsInjector {

    /*
     * Use a Bean to create the injectable claims object the first time it is asked for during an API request
     */
    @Bean
    @Scope(value = CustomRequestScope.NAME)
    public SampleApiClaims createClaims() {

        // Get claims from the security context
        var claims = SecurityContextProvider.getClaims(SampleApiClaims.class);
        if (claims != null) {
            return claims;
        }

        // For OPTIONS requests we may need to provide a default object to auto wire the controller
        return new SampleApiClaims();
    }
}
