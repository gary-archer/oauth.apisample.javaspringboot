package com.mycompany.sample.tests.utils;

import java.net.http.HttpResponse;
import lombok.Getter;

/*
 * Model a test API response
 */
public final class ApiResponse {

    public ApiResponse(final HttpResponse<String> response, final ApiResponseMetrics metrics) {
        this.statusCode = response.statusCode();
        this.body = response.body();
        this.metrics = metrics;
    }

    @Getter
    private final int statusCode;

    @Getter
    private final String body;

    @Getter
    private final ApiResponseMetrics metrics;
}
