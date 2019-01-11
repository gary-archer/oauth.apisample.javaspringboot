package com.mycompany.api.basicapi.logic;

import com.mycompany.api.basicapi.entities.UserInfoClaims;
import com.mycompany.api.basicapi.plumbing.oauth.ApiClaimsProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.CompletableFuture;

/*
 * A simple controller to return user info to the API once token authentication has completed
 */
@RestController
@RequestMapping(value = "api/userclaims")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserInfoController {

    /*
     * A testable way to access the security context
     */
    private final ApiClaimsProvider claimsProvider;

    /*
     * Receive the provider
     */
    public UserInfoController(ApiClaimsProvider claimsProvider) {
        this.claimsProvider = claimsProvider;
    }

    /*
     * Return the user info claims when the API is called
     */
    @GetMapping(value="current")
    public CompletableFuture<UserInfoClaims> GetUserClaims()
    {
        return CompletableFuture.completedFuture(this.claimsProvider.getApiClaims().getUserInfo());
    }
}
