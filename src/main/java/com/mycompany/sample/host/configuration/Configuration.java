package com.mycompany.sample.host.configuration;

import com.mycompany.sample.framework.api.base.configuration.FrameworkConfiguration;
import com.mycompany.sample.framework.api.oauth.configuration.OAuthConfiguration;
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
    private FrameworkConfiguration framework;

    @Getter
    @Setter
    private OAuthConfiguration oauth;
}
