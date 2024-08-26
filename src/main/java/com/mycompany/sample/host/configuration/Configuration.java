package com.authsamples.api.host.configuration;

import com.authsamples.api.plumbing.configuration.LoggingConfiguration;
import com.authsamples.api.plumbing.configuration.OAuthConfiguration;
import lombok.Getter;
import lombok.Setter;

/*
 * Represents the contents of our JSON configuration file
 */
public class Configuration {

    @Getter
    @Setter
    private ApiConfiguration api;

    @Getter
    @Setter
    private LoggingConfiguration logging;

    @Getter
    @Setter
    private OAuthConfiguration oauth;
}
