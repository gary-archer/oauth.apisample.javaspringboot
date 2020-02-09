package com.mycompany.sample.framework.api.base.logging;

import lombok.Getter;
import ch.qos.logback.classic.Level;

/*
 * A simple log level class
 */
public final class LogLevel {

    @Getter
    private final String name;

    @Getter
    private final Level level;

    public LogLevel(final String name, final Level level) {
        this.name = name;
        this.level = level;
    }
}
