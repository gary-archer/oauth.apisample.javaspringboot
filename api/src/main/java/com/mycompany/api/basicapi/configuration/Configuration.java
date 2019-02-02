package com.mycompany.api.basicapi.configuration;

import lombok.Getter;
import lombok.Setter;

/*
 * Represents the contents of our JSON configuration file
 */
public class Configuration {

    @Getter
    @Setter
    private AppConfiguration app;

    @Getter
    @Setter
    private OauthConfiguration oauth;
}
