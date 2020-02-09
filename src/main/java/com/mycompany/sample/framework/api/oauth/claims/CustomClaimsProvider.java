package com.mycompany.sample.framework.api.oauth.claims;

import com.mycompany.sample.framework.api.base.security.CoreApiClaims;
import javax.servlet.http.HttpServletRequest;

/*
 * An interface for providing custom claims
 */
public class CustomClaimsProvider<TClaims extends CoreApiClaims> {

    /*
     * Overridden by concrete classes to add custom claims
     */
    public void addCustomClaims(final String accessToken, final HttpServletRequest request, final TClaims claims) {
    }
}
