package com.authsamples.api.tests.utils;

import lombok.Getter;
import lombok.Setter;

/*
 * Options when calling an API
 */
public final class ApiRequestOptions {

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

    public ApiRequestOptions(final String accessToken) {
        this.accessToken = accessToken;
        this.rehearseException = false;
    }
}
