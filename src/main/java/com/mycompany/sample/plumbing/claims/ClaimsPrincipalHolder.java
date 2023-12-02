package com.mycompany.sample.plumbing.claims;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.mycompany.sample.plumbing.dependencies.CustomRequestScope;
import lombok.Getter;
import lombok.Setter;

/*
 * A holder object created when Spring initializes and updated per request after the authorizer runs
 */
@Component
@Scope(value = CustomRequestScope.NAME)
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class ClaimsPrincipalHolder {

    @Getter
    @Setter
    private ClaimsPrincipal claims;
}
