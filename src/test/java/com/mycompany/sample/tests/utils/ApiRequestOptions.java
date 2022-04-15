package com.mycompany.sample.tests.utils;

import lombok.Getter;
import lombok.Setter;

/*
 * Options when calling an API
 */
public class ApiRequestOptions {

    @Getter
    @Setter
    private String method;

    @Getter
    @Setter
    private String path;

    @Getter
    @Setter
    private String accessToken;
}
