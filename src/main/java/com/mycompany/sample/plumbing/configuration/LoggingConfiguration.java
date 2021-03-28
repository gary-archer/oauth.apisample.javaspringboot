package com.mycompany.sample.plumbing.configuration;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.sample.plumbing.utilities.UnderscorePropertyNamingStrategy;
import lombok.Getter;
import lombok.Setter;

/*
 * Logging configuration data
 */
@JsonNaming(UnderscorePropertyNamingStrategy.class)
public final class LoggingConfiguration {

    @Getter
    @Setter
    private String _apiName;

    @Getter
    @Setter
    private ObjectNode _production;

    @Getter
    @Setter
    private ObjectNode _development;
}
