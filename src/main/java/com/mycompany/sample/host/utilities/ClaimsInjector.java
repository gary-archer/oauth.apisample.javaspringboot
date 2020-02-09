package com.mycompany.sample.host.utilities;

import com.mycompany.sample.framework.api.base.security.SecurityContextProvider;
import com.mycompany.sample.host.claims.SampleApiClaims;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/*
 * A utility to inject the claims from the security context into business logic
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class ClaimsInjector {

    /*
     * Get an object to inject into CompanyRepository with the OAuth processing results
     */
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public SampleApiClaims getClaims() {
        return SecurityContextProvider.getClaims(SampleApiClaims.class);
    }
}
