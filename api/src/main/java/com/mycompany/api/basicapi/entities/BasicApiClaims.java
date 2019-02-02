package com.mycompany.api.basicapi.entities;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.api.basicapi.plumbing.oauth.CoreApiClaims;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;

/*
 * Override the core claims to support additional custom claims
 */
public class BasicApiClaims extends CoreApiClaims {

    // Product specific data for authorization
    @Getter
    @Setter
    private int[] accountsCovered;

    /*
     * Write class members to a JSON object when saving to the claims cache
     */
    public ObjectNode toJson() {

        // Let the base class serialize its fields
        var claimsJson = super.toJson();

        // Add our custom claims
        var accountsJson = claimsJson.putArray("accountsCovered");
        for(var account: accountsCovered) {
            accountsJson.add(account);
        }

        return claimsJson;
    }

    /*
     * Deserialize into class members when reading from the claims cache
     */
    public void fromJson(ObjectNode claimsJson) {

        // Let the base class deserialize its claims
        super.fromJson(claimsJson);

        // Deserialize our custom claims
        var accountsJson = claimsJson.get("accountsCovered");
        this.accountsCovered = new int[accountsJson.size()];
        for (var item = 0; item < this.accountsCovered.length; item++) {
            var account = accountsJson.get(item).asInt();
            this.accountsCovered[item] = account;
        }
    }
}
