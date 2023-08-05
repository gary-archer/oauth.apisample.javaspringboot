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
    private final String managerId;

    @Getter
    private final String role;

    @Getter
    private final String[] regions;

    /*
     * Called when claims are deserialized during claims caching
     */
    public static SampleCustomClaims importData(final JsonNode data) {

        var managerId = data.get("manager_id").asText();
        var role = data.get("role").asText();

        var regionsNode = (ArrayNode) data.get("regions");
        var regions = new ArrayList<String>();
        regionsNode.forEach((n) -> {
            regions.add(n.asText());
        });

        return new SampleCustomClaims(managerId, role, regions.toArray(String[]::new));
    }

    /*
     * Receive individual claims when getting claims from the cache
     */
    public SampleCustomClaims(final String managerId, final String role, final String[] regions) {

        this.managerId = managerId;
        this.role = role;
        this.regions = regions;
    }

    /*
     * Called when claims are serialized during claims caching
     */
    @Override
    public ObjectNode exportData() {

        var mapper = new ObjectMapper();
        var data = mapper.createObjectNode();
        data.put("manager_id", this.managerId);
        data.put("role", this.role);

        var regionsNode = mapper.createArrayNode();
        for (var region: this.regions) {
            regionsNode.add(region);
        }

        data.set("regions", regionsNode);
        return data;
    }
}
