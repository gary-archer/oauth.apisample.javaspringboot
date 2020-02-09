package com.mycompany.sample.logic.errors;

import lombok.Getter;

/*
 * A 4xx error type that is not REST specific and can be thrown from business logic
 */
public class BusinessError extends RuntimeException {

    @Getter
    private final String errorCode;

    public BusinessError(final String errorCode, final String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
