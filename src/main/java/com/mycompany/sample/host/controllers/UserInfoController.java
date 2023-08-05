package com.mycompany.sample.host.controllers;

import java.util.concurrent.CompletableFuture;

import com.mycompany.sample.plumbing.claims.ClaimsPrincipal;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.mycompany.sample.logic.entities.ClientUserInfo;
import com.mycompany.sample.logic.entities.SampleCustomClaims;
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
     * Return the user info claims when the API is called
     */
    @GetMapping(value = "")
    public CompletableFuture<ClientUserInfo> getUserClaims() {

        var userInfo = new ClientUserInfo();
        userInfo.setRole(this.customClaims.getUserRole());
        userInfo.setRegions(this.customClaims.getUserRegions());
        return CompletableFuture.completedFuture(userInfo);
    }
}
