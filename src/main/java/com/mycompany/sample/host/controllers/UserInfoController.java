package com.mycompany.sample.host.controllers;

import java.util.concurrent.CompletableFuture;
import com.mycompany.sample.plumbing.claims.ClaimsAccessor;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.mycompany.sample.logic.claims.SampleExtraClaims;
import com.mycompany.sample.logic.entities.ClientUserInfo;
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

    private final ClaimsAccessor claimsAccessor;

    /*
     * Claims are injected into the controller after OAuth processing
     */
    public UserInfoController(final ClaimsAccessor claimsAccessor) {
        this.claimsAccessor = claimsAccessor;
    }

    /*
     * Return user attributes that are not stored in the authorization server that the UI needs
     */
    @GetMapping(value = "")
    public CompletableFuture<ClientUserInfo> getUserInfo() {

        var extraClaims = (SampleExtraClaims) this.claimsAccessor.getMyPrincipal().getExtraClaims();

        var userInfo = new ClientUserInfo();
        userInfo.setTitle(extraClaims.getTitle());
        userInfo.setRegions(extraClaims.getRegions());
        return CompletableFuture.completedFuture(userInfo);
    }
}
