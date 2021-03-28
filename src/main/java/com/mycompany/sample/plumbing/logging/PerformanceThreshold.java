package com.mycompany.sample.plumbing.logging;

import lombok.Getter;

/*
 * A simple performance threshold container
 */
public final class PerformanceThreshold {

    @Getter
    private String _name;

    @Getter
    private int _milliseconds;

    public PerformanceThreshold(final String name, final int milliseconds) {
        this._name = name;
        this._milliseconds = milliseconds;
    }
}
