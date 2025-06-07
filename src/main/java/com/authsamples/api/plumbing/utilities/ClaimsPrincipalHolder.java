package com.authsamples.api.plumbing.utilities;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.authsamples.api.plumbing.claims.ClaimsPrincipal;
import com.authsamples.api.plumbing.dependencies.CustomRequestScope;
import lombok.Getter;
import lombok.Setter;

/*
 * A holder object initially created when Spring initializes
 * The custom authorizer updates its value during an API request
 */
@Component
@Scope(value = CustomRequestScope.NAME)
public class ClaimsPrincipalHolder<T> {

    @Getter
    @Setter
    private ClaimsPrincipal<T> claims;
}
