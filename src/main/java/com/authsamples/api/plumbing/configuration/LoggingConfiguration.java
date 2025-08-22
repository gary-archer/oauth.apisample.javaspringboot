package com.authsamples.api.plumbing.configuration;

import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Getter;
import lombok.Setter;

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
