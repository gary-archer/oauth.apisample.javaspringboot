package com.mycompany.sample.plumbing.logging;

import ch.qos.logback.classic.Level;
import lombok.Getter;

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
