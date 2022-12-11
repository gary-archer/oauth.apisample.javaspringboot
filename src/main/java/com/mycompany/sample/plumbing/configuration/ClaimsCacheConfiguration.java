package com.mycompany.sample.plumbing.configuration;

import lombok.Getter;
import lombok.Setter;

/*
 * Configuration related to claims caching in the API
 */
public class ClaimsCacheConfiguration {

    // The URL to the Authorization Server's user info endpoint, if needed
    @Getter
    @Setter
    private String userInfoEndpoint;

    // The maximum number of minutes for which to cache claims, when applicable
    @Getter
    @Setter
    private int timeToLiveMinutes;
}
