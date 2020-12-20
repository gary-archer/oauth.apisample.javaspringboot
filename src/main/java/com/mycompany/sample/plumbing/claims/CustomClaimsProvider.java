package com.mycompany.sample.plumbing.claims;

import javax.servlet.http.HttpServletRequest;

/*
 * An interface for providing custom claims
 */
@SuppressWarnings("PMD.GenericsNaming")
public class CustomClaimsProvider<TClaims extends CoreApiClaims> {

    /*
     * Overridden by concrete classes to add custom claims
     */
    @SuppressWarnings("PMD.UncommentedEmptyMethodBody")
    public void addCustomClaims(final String accessToken, final HttpServletRequest request, final TClaims claims) {
    }
}
