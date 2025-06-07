package com.authsamples.api.host.controllers;

import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.authsamples.api.logic.claims.ExtraClaims;
import com.authsamples.api.logic.entities.ClientUserInfo;
import com.authsamples.api.plumbing.dependencies.CustomRequestScope;
import com.authsamples.api.plumbing.utilities.ClaimsPrincipalHolder;

/*
 * Return user info from the business data to the client
 * These values are separate to the core identity data returned from the OAuth user info endpoint
 */
@RestController
@Scope(value = CustomRequestScope.NAME)
@RequestMapping(value = "investments/userinfo")
public class UserInfoController {

    private final ClaimsPrincipalHolder<ExtraClaims> claimsHolder;

    /*
     * The claims holder may be injected into the controller before OAuth processing
     * The OAuth filter then runs before any methods are called
     */
    public UserInfoController(final ClaimsPrincipalHolder<ExtraClaims> claimsHolder) {
        this.claimsHolder = claimsHolder;
    }

    /*
     * Return user attributes that are not stored in the authorization server to the client
     */
    @GetMapping(value = "")
    public ClientUserInfo getUserInfo() {

        var extraClaims = this.claimsHolder.getClaims().getExtraClaims();

        var userInfo = new ClientUserInfo();
        userInfo.setTitle(extraClaims.getTitle());
        userInfo.setRegions(extraClaims.getRegions());
        return userInfo;
    }
}
