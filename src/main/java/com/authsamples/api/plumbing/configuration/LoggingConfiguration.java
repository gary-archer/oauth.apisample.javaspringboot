package com.authsamples.api.plumbing.configuration;

import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.node.ArrayNode;

/*
 * Logging configuration data
 */
public final class LoggingConfiguration {

    @Getter
    @Setter
    private String apiName;

    @Getter
    @Setter
    private ArrayNode loggers;
}
