package com.mycompany.sample.host.controllers;

import java.util.concurrent.CompletableFuture;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.mycompany.sample.host.claims.SampleApiClaims;
import com.mycompany.sample.host.claims.UserInfoClaims;
import com.mycompany.sample.plumbing.dependencies.CustomRequestScope;

/*
 * A simple controller to return user info to the caller
 */
@RestController
@Scope(value = CustomRequestScope.NAME)
@RequestMapping(value = "api/userclaims")
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class UserInfoController {

    private final SampleApiClaims claims;

    /*
     * The claims object is injected into the controller or other classes after OAuth processing
     */
    public UserInfoController(final SampleApiClaims claims) {
        this.claims = claims;
    }

    /*
     * Return the user info claims when the API is called
     */
    @GetMapping(value = "current")
    public CompletableFuture<UserInfoClaims> getUserClaims() {

        var userInfo = new UserInfoClaims(
                this.claims.getGivenName(),
                this.claims.getFamilyName(),
                this.claims.getEmail());
        return CompletableFuture.completedFuture(userInfo);
    }
}
