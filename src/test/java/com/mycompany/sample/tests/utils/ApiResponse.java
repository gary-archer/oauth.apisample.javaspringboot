package com.mycompany.sample.tests.utils;

import java.net.http.HttpResponse;
import lombok.Getter;

/*
 * Model a test API response
 */
public class ApiResponse {

    public ApiResponse(final HttpResponse<String> response) {
        this.statusCode = response.statusCode();
        this.body = response.body();
    }

    @Getter
    private int statusCode;

    @Getter
    private String body;
}
