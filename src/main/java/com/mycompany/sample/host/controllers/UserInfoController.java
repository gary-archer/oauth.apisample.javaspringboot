package com.mycompany.sample.host.controllers;

import java.util.concurrent.CompletableFuture;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.mycompany.sample.logic.entities.ClientUserInfo;
import com.mycompany.sample.plumbing.dependencies.CustomRequestScope;
import com.mycompany.sample.plumbing.utilities.UserInfoClaimsResolver;

/*
 * A simple controller to return user info to the caller
 */
@RestController
@Scope(value = CustomRequestScope.NAME)
@RequestMapping(value = "api/userclaims")
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class UserInfoController {

    private final UserInfoClaimsResolver claimsResolver;

    /*
     * The claims resolver is injected into the controller after OAuth processing
     */
    public UserInfoController(final UserInfoClaimsResolver claimsResolver) {
        this.claimsResolver = claimsResolver;
    }

    /*
     * Return the user info claims when the API is called
     */
    @GetMapping(value = "current")
    public CompletableFuture<ClientUserInfo> getUserClaims() {

        var claims = this .claimsResolver.getUserInfoClaims();

        var userInfo = new ClientUserInfo();
        userInfo.setGivenName(claims.getGivenName());
        userInfo.setFamilyName(claims.getFamilyName());

        return CompletableFuture.completedFuture(userInfo);
    }
}
