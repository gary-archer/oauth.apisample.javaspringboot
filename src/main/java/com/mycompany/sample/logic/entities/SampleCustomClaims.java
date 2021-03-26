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
    private final String userId;

    @Getter
    private final String userRole;

    @Getter
    private final String[] userRegions;

    public static SampleCustomClaims importData(final JsonNode data) {

        var userDatabaseId = data.get("userId").asText();
        var userRole = data.get("userRole").asText();
        var userRegions = data.get("userRegions").asText();
        return new SampleCustomClaims(userDatabaseId, userRole, userRegions.split(" "));
    }

    public SampleCustomClaims(
            final String userId,
            final String userRole,
            final String[] userRegions) {

        this.userId = userId;
        this.userRole = userRole;
        this.userRegions = userRegions;
    }

    @Override
    public ObjectNode exportData() {

        var mapper = new ObjectMapper();
        var data = mapper.createObjectNode();
        data.put("userId", this.userId);
        data.put("userRole", this.userRole);
        data.put("userRegions", String.join(" ", this.userRegions));
        return data;
    }
}
