package com.mycompany.sample.host.controllers;

import java.util.concurrent.CompletableFuture;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.mycompany.sample.logic.entities.SampleCustomClaims;
import com.mycompany.sample.plumbing.claims.ClaimsPrincipal;
import com.mycompany.sample.plumbing.claims.CustomClaims;
import com.mycompany.sample.plumbing.dependencies.CustomRequestScope;

/*
 * Return user info from the business data to the client
 * Clients call the authorization server's user info endpoint to get OAuth user attributes
 */
@RestController
@Scope(value = CustomRequestScope.NAME)
@RequestMapping(value = "investments/userinfo")
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class UserInfoController {

    private final SampleCustomClaims customClaims;

    /*
     * Claims are injected into the controller after OAuth processing
     */
    public UserInfoController(final ClaimsPrincipal claims) {
        this.customClaims = (SampleCustomClaims) claims.getCustomClaims();
    }

    /*
     * Some or all of these values can be returned to API clients when required
     */
    @GetMapping(value = "")
    public CompletableFuture<CustomClaims> getUserInfo() {
        return CompletableFuture.completedFuture(this.customClaims);
    }
}
