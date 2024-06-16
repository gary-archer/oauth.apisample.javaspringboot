package com.authsamples.api.plumbing.claims;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/*
 * Claims that you cannot, or do not want to, manage in the authorization server
 */
public class ExtraClaims {

    public ExtraClaims() {
    }

    public static ExtraClaims importData(final JsonNode data) {
        return new ExtraClaims();
    }

    /*
     * This can be overridden by derived classes when saving claims to a cache
     */
    @SuppressWarnings(value = "checkstyle:DesignForExtension")
    public ObjectNode exportData() {
        var mapper = new ObjectMapper();
        return mapper.createObjectNode();
    }
}
