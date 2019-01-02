package com.mycompany.api.basicapi.logic;

import com.mycompany.api.basicapi.entities.UserInfoClaims;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(value = "api/userclaims")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserInfoController {

    /*
     * Return some user claims
     */
    @GetMapping(value="current")
    public CompletableFuture<UserInfoClaims> GetUserClaims()
    {
        UserInfoClaims userInfo = new UserInfoClaims() {{
            givenName = "Guest";
            familyName = "User";
            email = "guestuser@authguidance.com";
        }};

        return CompletableFuture.completedFuture(userInfo);
    }
}
