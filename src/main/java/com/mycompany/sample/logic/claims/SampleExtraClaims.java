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
        var title = data.get(CustomClaimNames.Title).asText();
        var regionsNode = (ArrayNode) data.get(CustomClaimNames.Regions);
        var regions = new ArrayList<String>();
        regionsNode.forEach((n) -> {
            regions.add(n.asText());
        });
        var claims = new SampleExtraClaims(title, regions.toArray(String[]::new));

        // These are only stored in the cache when the authorization server cannot issue them to access tokens
        var managerIdNode = data.get(CustomClaimNames.ManagerId);
        var roleNode = data.get(CustomClaimNames.Role);
        if (managerIdNode != null && roleNode != null) {

            var managerId = managerIdNode.asText();
            var role = roleNode.asText();
            claims.addCoreClaims(managerId, role);
        }

        return claims;
    }

    /*
     * Construct with Claims that are always looked up from the API's own data
     */
    public SampleExtraClaims(final String title, final String[] regions) {
        this.title = title;
        this.regions = regions;
    }

    /*
     * These values should be issued to the access token and store in the JWT claims
     * When not supported by the authorization server they are stored in this class instead
     */
    public void addCoreClaims(final String managerId, final String role) {
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
        data.put(CustomClaimNames.Title, this.title);
        var regionsNode = mapper.createArrayNode();
        for (var region: this.regions) {
            regionsNode.add(region);
        }
        data.set(CustomClaimNames.Regions, regionsNode);

        // These are only stored in the cache when the authorization server cannot issue them to access tokens
        if (this.managerId != null && this.role != null) {
            data.put(CustomClaimNames.ManagerId, this.managerId);
            data.put(CustomClaimNames.Role, this.role);
        }

        return data;
    }
}
