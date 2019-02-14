package com.mycompany.api.basicapi.plumbing.oauth;

import lombok.Getter;
import lombok.Setter;

/*
 * OAuth configuration data
 */
public class OauthConfiguration {

    @Getter
    @Setter
    private String authority;

    @Getter
    @Setter
    private String clientId;

    @Getter
    @Setter
    private String clientSecret;

    @Getter
    @Setter
    private int defaultTokenCacheMinutes;
}
