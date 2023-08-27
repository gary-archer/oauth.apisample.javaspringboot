package com.mycompany.sample.logic.claims;

import java.util.ArrayList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.sample.plumbing.claims.ExtraClaims;
import lombok.Getter;

/*
 * Represents extra claims not received in access tokens
 */
public final class SampleExtraClaims extends ExtraClaims {

    @Getter
    private final String title;

    @Getter
    private final String[] regions;

    @Getter
    private String managerId;

    @Getter
    private String role;

    /*
     * Called when claims are deserialized during claims caching
     */
    public static SampleExtraClaims importData(final JsonNode data) {

        // These claims are always stored in the cache
        var title = data.get("title").asText();
        var regionsNode = (ArrayNode) data.get("regions");
        var regions = new ArrayList<String>();
        regionsNode.forEach((n) -> {
            regions.add(n.asText());
        });
        var claims = new SampleExtraClaims(title, regions.toArray(String[]::new));

        // These are only stored in the cache when the authorization server cannot issue them to access tokens
        var managerIdNode = data.get("manager_id");
        var roleNode = data.get("role");
        if (managerIdNode != null && roleNode != null) {

            var managerId = managerIdNode.asText();
            var role = roleNode.asText();
            claims.addTokenClaims(managerId, role);
        }

        return claims;
    }

    /*
     * Claims that are always cached
     */
    public SampleExtraClaims(final String title, final String[] regions) {
        this.title = title;
        this.regions = regions;
    }

    /*
     * Claims that are only cached when they cannot be issued to access tokens
     */
    public void addTokenClaims(final String managerId, final String role) {
        this.managerId = managerId;
        this.role = role;
    }

    /*
     * Called when claims are serialized during claims caching
     */
    @Override
    public ObjectNode exportData() {

        var mapper = new ObjectMapper();
        var data = mapper.createObjectNode();

        // These claims are always stored in the cache
        data.put("title", this.title);
        var regionsNode = mapper.createArrayNode();
        for (var region: this.regions) {
            regionsNode.add(region);
        }
        data.set("regions", regionsNode);

        // These are only stored in the cache when the authorization server cannot issue them to access tokens
        if (this.managerId != null && this.role != null) {
            data.put("manager_id", this.managerId);
            data.put("role", this.role);
        }

        return data;
    }
}
