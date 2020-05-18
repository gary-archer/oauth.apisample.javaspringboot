package com.mycompany.sample.host.plumbing.configuration;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

/*
 * Logging configuration data
 */
public final class LoggingConfiguration {

    @Getter
    @Setter
    private ObjectNode production;

    @Getter
    @Setter
    private ObjectNode development;
}
