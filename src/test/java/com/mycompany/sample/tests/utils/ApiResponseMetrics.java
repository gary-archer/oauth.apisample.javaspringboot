package com.mycompany.sample.tests.utils;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/*
 * Some metrics once an API call completes
 */
public class ApiResponseMetrics {

    public ApiResponseMetrics(final String operation) {
        this.operation = operation;
        this.startTime = null;
        this.correlationId = "";
        this.millisecondsTaken = 0;
    }

    @Getter
    private String operation;

    @Getter
    @Setter
    private Instant startTime;

    @Getter
    @Setter
    private String correlationId;

    @Getter
    @Setter
    private long millisecondsTaken;
}
