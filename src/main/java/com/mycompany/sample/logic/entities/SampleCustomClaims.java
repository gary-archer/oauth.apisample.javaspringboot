package com.mycompany.sample.logic.entities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.sample.plumbing.claims.CustomClaims;
import lombok.Getter;

/*
 * Extend core claims for this particular API
 */
public final class SampleCustomClaims extends CustomClaims {

    @Getter
    private final String userDatabaseId;

    @Getter
    private final boolean isAdmin;

    @Getter
    private final String[] regionsCovered;

    public static SampleCustomClaims importData(final JsonNode data) {

        var userDatabaseId = data.get("userDatabaseId").asText();
        var isAdmin = data.get("isAdmin").asBoolean();
        var regionsCovered = data.get("regionsCovered").asText();
        return new SampleCustomClaims(userDatabaseId, isAdmin, regionsCovered.split(" "));
    }

    public SampleCustomClaims(
            final String userDatabaseId,
            final boolean isAdmin,
            final String[] regionsCovered) {

        this.userDatabaseId = userDatabaseId;
        this.isAdmin = isAdmin;
        this.regionsCovered = regionsCovered;
    }

    @Override
    public ObjectNode exportData() {

        var mapper = new ObjectMapper();
        var data = mapper.createObjectNode();
        data.put("userDatabaseId", this.userDatabaseId);
        data.put("isAdmin", this.isAdmin);
        data.put("regionsCovered", String.join(" ", this.regionsCovered));
        return data;
    }
}
