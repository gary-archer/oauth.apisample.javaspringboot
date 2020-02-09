package com.mycompany.sample.framework.api.base.logging;

import lombok.Getter;

/*
 * A simple performance threshold container
 */
public final class PerformanceThreshold {

    @Getter
    private String name;

    @Getter
    private int milliseconds;

    public PerformanceThreshold(final String name, final int milliseconds) {
        this.name = name;
        this.milliseconds = milliseconds;
    }
}
