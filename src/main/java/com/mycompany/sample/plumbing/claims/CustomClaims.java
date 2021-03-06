package com.mycompany.sample.plumbing.claims;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/*
 * Custom claims, which are empty by default
 */
public class CustomClaims {

    public CustomClaims() {
    }

    public static CustomClaims importData(final JsonNode data) {
        return new CustomClaims();
    }

    /*
     * This can be overridden by derived classes to export real custom claims
     */
    @SuppressWarnings(value = "checkstyle:DesignForExtension")
    public ObjectNode exportData() {

        var mapper = new ObjectMapper();
        return mapper.createObjectNode();
    }
}
