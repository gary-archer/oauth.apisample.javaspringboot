package com.authsamples.api.tests.utils;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/*
 * Some metrics once an API call completes
 */
public class ApiResponseMetrics {

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

    public ApiResponseMetrics(final String operation) {
        this.operation = operation;
        this.startTime = null;
        this.correlationId = "";
        this.millisecondsTaken = 0;
    }
}
