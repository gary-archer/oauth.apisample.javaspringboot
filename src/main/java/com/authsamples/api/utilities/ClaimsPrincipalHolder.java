package com.authsamples.api.utilities;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.authsamples.api.dependencies.CustomRequestScope;
import com.authsamples.api.middleware.claims.ClaimsPrincipal;
import lombok.Getter;
import lombok.Setter;

/*
 * A holder object initially created when Spring initializes
 * The custom authorizer updates its value during an API request
 */
@Component
@Scope(value = CustomRequestScope.NAME)
public class ClaimsPrincipalHolder {

    @Getter
    @Setter
    private ClaimsPrincipal claims;
}
