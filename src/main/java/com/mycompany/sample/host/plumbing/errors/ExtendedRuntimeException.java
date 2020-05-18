package com.mycompany.sample.host.plumbing.errors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;

/*
 * An error class with no REST dependencies which receives an error code, short message and details
 */
public final class ExtendedRuntimeException extends RuntimeException {

    @Getter
    private final String errorCode;

    @Getter
    private ObjectNode details;

    public ExtendedRuntimeException(final String errorCode, final String userMessage, final Throwable ex) {
        super(userMessage, ex);
        this.errorCode = errorCode;
    }

    public void setDetails(final String details) {

        var mapper = new ObjectMapper();
        this.details = mapper.createObjectNode();
        this.details.put("data", details);
    }

    public void setDetails(final ObjectNode details) {
        this.details = details;
    }
}
