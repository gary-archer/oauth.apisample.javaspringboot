package com.mycompany.sample.host.configuration;

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
    private OAuthConfiguration oauth;

    @Getter
    @Setter
    private FrameworkConfiguration framework;
}
