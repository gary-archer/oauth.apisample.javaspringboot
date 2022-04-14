package com.mycompany.sample.tests.utils;

import com.mycompany.sample.plumbing.claims.UserInfoClaims;

public final class ApiClient {

    private final String baseUrl;

    public ApiClient(final String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public UserInfoClaims getUserInfoClaims(final String accessToken) {
        return new UserInfoClaims("Guest", "User", "guestuser@mycompany.com");
    }
}
