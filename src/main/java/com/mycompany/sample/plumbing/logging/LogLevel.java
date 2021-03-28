package com.mycompany.sample.plumbing.logging;

import ch.qos.logback.classic.Level;
import lombok.Getter;

/*
 * A simple log level class
 */
public final class LogLevel {

    @Getter
    private final String _name;

    @Getter
    private final Level _level;

    public LogLevel(final String name, final Level level) {
        this._name = name;
        this._level = level;
    }
}
