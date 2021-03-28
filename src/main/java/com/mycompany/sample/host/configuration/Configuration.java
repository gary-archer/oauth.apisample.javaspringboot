package com.mycompany.sample.host.configuration;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.mycompany.sample.plumbing.configuration.LoggingConfiguration;
import com.mycompany.sample.plumbing.configuration.OAuthConfiguration;
import com.mycompany.sample.plumbing.utilities.UnderscorePropertyNamingStrategy;
import lombok.Getter;
import lombok.Setter;

/*
 * Represents the contents of our JSON configuration file
 */
@JsonNaming(UnderscorePropertyNamingStrategy.class)
public class Configuration {

    @Getter
    @Setter
    private ApiConfiguration _api;

    @Getter
    @Setter
    private LoggingConfiguration _logging;

    @Getter
    @Setter
    private OAuthConfiguration _oauth;
}
