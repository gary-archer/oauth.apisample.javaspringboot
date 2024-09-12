package com.authsamples.api.logic.claims;

import java.util.ArrayList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.authsamples.api.plumbing.claims.ExtraClaims;
import lombok.Getter;

/*
 * Represents extra claims not received in access tokens
 */
public final class SampleExtraClaims extends ExtraClaims {

    @Getter
    private final String title;

    @Getter
    private final String[] regions;

    /*
     * Called when claims are deserialized during claims caching
     */
    public static SampleExtraClaims importData(final JsonNode data) {

        var title = data.get(CustomClaimNames.Title).asText();
        var regionsNode = (ArrayNode) data.get(CustomClaimNames.Regions);
        var regions = new ArrayList<String>();
        regionsNode.forEach((n) -> {
            regions.add(n.asText());
        });
        return new SampleExtraClaims(title, regions.toArray(String[]::new));
    }

    /*
     * Construct with Claims that are always looked up from the API's own data
     */
    public SampleExtraClaims(final String title, final String[] regions) {
        this.title = title;
        this.regions = regions;
    }

    /*
     * Called when claims are serialized during claims caching
     */
    @Override
    public ObjectNode exportData() {

        var mapper = new ObjectMapper();
        var data = mapper.createObjectNode();

        data.put(CustomClaimNames.Title, this.title);
        var regionsNode = mapper.createArrayNode();
        for (var region: this.regions) {
            regionsNode.add(region);
        }
        data.set(CustomClaimNames.Regions, regionsNode);
        return data;
    }
}
