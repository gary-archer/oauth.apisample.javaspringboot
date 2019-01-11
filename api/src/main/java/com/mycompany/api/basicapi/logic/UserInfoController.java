package com.mycompany.api.basicapi.logic;

import com.mycompany.api.basicapi.entities.ApiClaims;
import com.mycompany.api.basicapi.entities.UserInfoClaims;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(value = "api/userclaims")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserInfoController {

    /*
     * Return the user claims from the security context
     */
    @GetMapping(value="current")
    public CompletableFuture<UserInfoClaims> GetUserClaims()
    {
        ApiClaims claims = ((ApiClaims) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        return CompletableFuture.completedFuture(claims.getUserInfo());
    }
}
