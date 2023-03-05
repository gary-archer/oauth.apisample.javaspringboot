package com.mycompany.sample.host.controllers;

import java.util.concurrent.CompletableFuture;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.mycompany.sample.logic.entities.ClientUserInfo;
import com.mycompany.sample.logic.entities.SampleCustomClaims;
import com.mycompany.sample.plumbing.claims.BaseClaims;
import com.mycompany.sample.plumbing.claims.CustomClaims;
import com.mycompany.sample.plumbing.claims.UserInfoClaims;
import com.mycompany.sample.plumbing.dependencies.CustomRequestScope;
import com.mycompany.sample.plumbing.oauth.ScopeVerifier;

/*
 * A simple controller to return user info to the caller
 */
@RestController
@Scope(value = CustomRequestScope.NAME)
@RequestMapping(value = "investments/userinfo")
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class UserInfoController {

    private final BaseClaims baseClaims;
    private final UserInfoClaims userInfoClaims;
    private final SampleCustomClaims customClaims;

    /*
     * Claims are injected into the controller after OAuth processing
     */
    public UserInfoController(
            final BaseClaims baseClaims,
            final UserInfoClaims userInfoClaims,
            final CustomClaims customClaims) {

        this.baseClaims = baseClaims;
        this.userInfoClaims = userInfoClaims;
        this.customClaims = (SampleCustomClaims) customClaims;
    }

    /*
     * Return the user info claims when the API is called
     */
    @GetMapping(value = "")
    public CompletableFuture<ClientUserInfo> getUserClaims() {

        // First check scopes
        ScopeVerifier.enforce(this.baseClaims.getScopes(), "profile");

        // Next return the user info
        var userInfo = new ClientUserInfo();
        userInfo.setGivenName(this.userInfoClaims.getGivenName());
        userInfo.setFamilyName(this.userInfoClaims.getFamilyName());
        userInfo.setRegions(this.customClaims.getUserRegions());
        return CompletableFuture.completedFuture(userInfo);
    }
}
