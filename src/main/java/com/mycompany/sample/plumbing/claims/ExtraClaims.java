package com.mycompany.sample.plumbing.claims;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/*
 * Represents finer grained business permissions that you do not want to issue to access tokens
 * Such values are usually best managed in the business data and not the authorization server
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
