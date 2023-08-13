package com.mycompany.sample.host.controllers;

import java.util.concurrent.CompletableFuture;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.mycompany.sample.logic.entities.ClientUserInfo;
import com.mycompany.sample.logic.entities.SampleExtraClaims;
import com.mycompany.sample.plumbing.claims.ClaimsPrincipal;
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

    private final SampleExtraClaims extraClaims;

    /*
     * Claims are injected into the controller after OAuth processing
     */
    public UserInfoController(final ClaimsPrincipal claims) {
        this.extraClaims = (SampleExtraClaims) claims.getExtraClaims();
    }

    /*
     * Return user attributes that are not stored in the authorization server that the UI needs
     */
    @GetMapping(value = "")
    public CompletableFuture<ClientUserInfo> getUserInfo() {
        var userInfo = new ClientUserInfo();
        userInfo.setRole(this.extraClaims.getRole());
        userInfo.setRegions(this.extraClaims.getRegions());
        return CompletableFuture.completedFuture(userInfo);
    }
}
