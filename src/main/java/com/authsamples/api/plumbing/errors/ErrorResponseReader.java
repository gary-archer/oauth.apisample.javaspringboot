package com.authsamples.api.plumbing.errors;

import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

public final class ErrorResponseReader {

    private ErrorResponseReader() {
    }

    /*
     * A utility to read JSON and ignore errors, to make the calling code simpler
     * This prevents 'double faults' during error handling
     */
    public static ObjectNode tryReadJson(final String jsonText) {

        if (!StringUtils.hasLength(jsonText)) {
            return null;
        }

        try {
            var mapper = new ObjectMapper();
            return mapper.readValue(jsonText, ObjectNode.class);

        } catch (Throwable ignored) {
            return null;
        }
    }
}
