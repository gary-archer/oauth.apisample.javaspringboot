package com.mycompany.sample.tests.utils;

import lombok.Getter;
import lombok.Setter;

/*
 * Options when calling an API
 */
public class ApiRequestOptions {

    public ApiRequestOptions(String accessToken) {
        this.accessToken = accessToken;
        this.rehearseException = false;
    }

    @Getter
    private String accessToken;

    @Getter
    @Setter
    private String method;

    @Getter
    @Setter
    private String path;

    @Getter
    @Setter
    private Boolean rehearseException;
}
