package com.mycompany.sample.logic.entities;

import java.util.ArrayList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.sample.plumbing.claims.ExtraClaims;
import lombok.Getter;

/*
 * Some example claims that may not be present in the access token
 * In some cases this may be due to authorization server limitations
 * In other cases they may be easier to manage outside the authorization server
 * The API's service logic treats such values as claims though
 */
public final class SampleExtraClaims extends ExtraClaims {

    @Getter
    private final String managerId;

    @Getter
    private final String role;

    @Getter
    private final String[] regions;

    /*
     * Called when claims are deserialized during claims caching
     */
    public static SampleExtraClaims importData(final JsonNode data) {

        var managerId = data.get("manager_id").asText();
        var role = data.get("role").asText();

        var regionsNode = (ArrayNode) data.get("regions");
        var regions = new ArrayList<String>();
        regionsNode.forEach((n) -> {
            regions.add(n.asText());
        });

        return new SampleExtraClaims(managerId, role, regions.toArray(String[]::new));
    }

    /*
     * Receive individual claims when getting claims from the cache
     */
    public SampleExtraClaims(final String managerId, final String role, final String[] regions) {

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
