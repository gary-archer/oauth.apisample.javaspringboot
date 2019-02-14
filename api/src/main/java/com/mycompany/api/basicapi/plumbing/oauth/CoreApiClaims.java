package com.mycompany.api.basicapi.plumbing.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import org.springframework.security.core.AuthenticatedPrincipal;

/*
 * A base class for API claims
 */
public class CoreApiClaims implements AuthenticatedPrincipal {

    // The immutable user id from the access token, which may exist in the API's database
    @Getter
    private String userId;

    // The client id, which typically represents the calling application
    @Getter
    private String clientId;

    // OAuth scopes can represent high level areas of the business
    @Getter
    private String[] scopes;

    // User info fields
    @Getter
    private String givenName;

    @Getter
    private String familyName;

    @Getter
    private String email;

    // We return the immutable user id from the access token as the user name, which is not a display value
    @Override
    public String getName() {
        return userId;
    }

    /*
     * Set token claims after introspection
     */
    public void setTokenInfo(String userId, String clientId, String[] scopes) {
        this.userId = userId;
        this.clientId = clientId;
        this.scopes = scopes;
    }

    /*
     * Set informational fields after user info lookup
     */
    public void setCentralUserInfo(String givenName, String familyName, String email) {
        this.givenName = givenName;
        this.familyName = familyName;
        this.email = email;
    }

    /*
     * Write class members to a JSON object when saving to the claims cache
     */
    public ObjectNode toJson() {
        var mapper = new ObjectMapper();
        var claimsJson = mapper.createObjectNode();

        claimsJson.put("userId", this.userId);
        claimsJson.put("clientId", this.clientId);
        var scopesJson = claimsJson.putArray("scopes");
        for (var scope: this.scopes) {
            scopesJson.add(scope);
        }

        claimsJson.put("givenName", this.givenName);
        claimsJson.put("familyName", this.familyName);
        claimsJson.put("email", this.email);
        return claimsJson;
    }

    /*
     * Deserialize into class members when reading from the claims cache
     */
    public void fromJson(ObjectNode claimsJson) {

        this.userId = claimsJson.get("userId").asText();
        this.clientId = claimsJson.get("clientId").asText();

        var scopesJson = claimsJson.get("scopes");
        this.scopes = new String[scopesJson.size()];
        for (var item = 0; item < scopes.length; item++) {
            var scope = scopesJson.get(item).asText();
            this.scopes[item] = scope;
        }

        this.givenName = claimsJson.get("givenName").asText();
        this.familyName = claimsJson.get("familyName").asText();
        this.email = claimsJson.get("email").asText();
    }
}
