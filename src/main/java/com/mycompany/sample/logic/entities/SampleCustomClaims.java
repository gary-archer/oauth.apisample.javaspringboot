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

    /*
     * Called when claims are deserialized during claims caching
     */
    public static SampleCustomClaims importData(final JsonNode data) {

        var userDatabaseId = data.get("user_id").asText();
        var userRole = data.get("user_role").asText();

        var regionsNode = (ArrayNode) data.get("user_regions");
        var userRegions = new ArrayList<String>();
        regionsNode.forEach((n) -> {
            userRegions.add(n.asText());
        });

        return new SampleCustomClaims(userDatabaseId, userRole, userRegions.toArray(String[]::new));
    }

    /*
     * Receive individual claims when getting claims from the cache
     */
    public SampleCustomClaims(final String userId, final String userRole, final String[] userRegions) {

        this.userId = userId;
        this.userRole = userRole;
        this.userRegions = userRegions;
    }

    /*
     * Called when claims are serialized during claims caching
     */
    @Override
    public ObjectNode exportData() {

        var mapper = new ObjectMapper();
        var data = mapper.createObjectNode();
        data.put("user_id", this.userId);
        data.put("user_role", this.userRole);

        var regionsNode = mapper.createArrayNode();
        for (var region: this.userRegions) {
            regionsNode.add(region);
        }

        data.set("user_regions", regionsNode);
        return data;
    }
}
