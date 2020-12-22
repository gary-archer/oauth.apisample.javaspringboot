package com.mycompany.sample.host.controllers;

import java.util.concurrent.CompletableFuture;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.mycompany.sample.host.claims.UserInfoClaims;
import com.mycompany.sample.host.utilities.ClaimsResolver;
import com.mycompany.sample.plumbing.dependencies.CustomRequestScope;
import com.mycompany.sample.plumbing.security.CustomAuthentication;

/*
 * A simple controller to return user info to the caller
 */
@RestController
@Scope(value = CustomRequestScope.NAME)
@RequestMapping(value = "api/userclaims")
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class UserInfoController {

    private final ClaimsResolver claimsResolver;

    /*
     * The claims resolver is injected into the controller after OAuth processing
     */
    public UserInfoController(final ClaimsResolver claimsResolver) {
        this.claimsResolver = claimsResolver;
    }

    /*
     * Return the user info claims when the API is called
     */
    @GetMapping(value = "current")
    public CompletableFuture<UserInfoClaims> getUserClaims(final CustomAuthentication principal) {

        var claims = this .claimsResolver.getClaims();

        var userInfo = new UserInfoClaims(
                claims.getGivenName(),
                claims.getFamilyName(),
                claims.getEmail());

        return CompletableFuture.completedFuture(userInfo);
    }
}
