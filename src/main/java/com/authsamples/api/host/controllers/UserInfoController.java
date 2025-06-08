package com.authsamples.api.host.controllers;

import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.authsamples.api.logic.entities.ClientUserInfo;
import com.authsamples.api.plumbing.dependencies.CustomRequestScope;
import com.authsamples.api.plumbing.utilities.ClaimsPrincipalHolder;

/*
 * This user info is separate to the OpenID Connect user info that returns core user attributes
 */
@RestController
@Scope(value = CustomRequestScope.NAME)
@RequestMapping(value = "investments/userinfo")
public class UserInfoController {

    private final ClaimsPrincipalHolder claimsHolder;

    /*
     * The claims holder may be injected into the controller before the OAuth filter runs
     * The OAuth filter then runs before any methods are called
     */
    public UserInfoController(final ClaimsPrincipalHolder claimsHolder) {
        this.claimsHolder = claimsHolder;
    }

    /*
     * Return product specific user info from the API to clients
     */
    @GetMapping(value = "")
    public ClientUserInfo getUserInfo() {

        var extraValues = this.claimsHolder.getClaims().getExtraValues();

        var userInfo = new ClientUserInfo();
        userInfo.setTitle(extraValues.getTitle());
        userInfo.setRegions(extraValues.getRegions());
        return userInfo;
    }
}
