package com.mycompany.sample.plumbing.claims;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/*
 * Custom claims, which are empty by default
 */
public class CustomClaims {

    public CustomClaims() {
    }

    public CustomClaims importData(ObjectNode data) {
        return new CustomClaims();
    }

    public ObjectNode exportData(ApiClaims claims) {
        var mapper = new ObjectMapper();
        return mapper.createObjectNode();
    }
}
