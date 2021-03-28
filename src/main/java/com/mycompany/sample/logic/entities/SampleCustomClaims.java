package com.mycompany.sample.logic.entities;

import java.util.ArrayList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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

        var regionsNode = (ArrayNode) data.get("userRegions");
        var userRegions = new ArrayList<String>();
        regionsNode.forEach((n) -> {
            userRegions.add(n.asText());
        });

        return new SampleCustomClaims(userDatabaseId, userRole, userRegions.toArray(String[]::new));
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

        var regionsNode = mapper.createArrayNode();
        for (var region: this.userRegions) {
            regionsNode.add(region);
        }

        data.set("userRegions", regionsNode);
        return data;
    }
}
