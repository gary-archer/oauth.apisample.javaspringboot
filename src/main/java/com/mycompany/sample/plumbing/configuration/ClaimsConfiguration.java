package com.mycompany.sample.plumbing.configuration;

import lombok.Getter;
import lombok.Setter;

/*
 * OAuth configuration settings
 */
public class ClaimsConfiguration {

    @Getter
    @Setter
    private int maxCacheMinutes;
}
