package com.authsamples.api.plumbing.claims;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.authsamples.api.plumbing.dependencies.CustomRequestScope;
import lombok.Getter;
import lombok.Setter;

/*
 * A holder object initially created when Spring initializes
 * The custom authorizer updates its value during an API request
 * The updated value can then be accessed from any async thread during the request lifecycle
 */
@Component
@Scope(value = CustomRequestScope.NAME)
public class ClaimsPrincipalHolder {

    @Getter
    @Setter
    private ClaimsPrincipal claims;
}
