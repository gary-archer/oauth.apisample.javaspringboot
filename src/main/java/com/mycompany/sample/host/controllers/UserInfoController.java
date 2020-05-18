package com.mycompany.sample.host.controllers;

import com.mycompany.sample.host.claims.SampleApiClaims;
import com.mycompany.sample.host.claims.UserInfoClaims;
import java.util.concurrent.CompletableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;

/*
 * A simple controller to return user info to the caller
 */
@RestController
@RequestScope
@RequestMapping(value = "api/userclaims")
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class UserInfoController {

    private final SampleApiClaims claims;

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
