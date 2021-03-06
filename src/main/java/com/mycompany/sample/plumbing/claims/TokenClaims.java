package com.mycompany.sample.plumbing.claims;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;

/*
 * Claims included in the JWT
 */
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class TokenClaims {

    @Getter
    private final String subject;

    @Getter
    private final String clientId;

    @Getter
    private final String[] scopes;

    @Getter
    private final int expiry;

    public static TokenClaims importData(final JsonNode data) {

        var subjectValue = data.get("subject").asText();
        var clientIdValue = data.get("clientId").asText();
        var scopeValue = data.get("scopes").asText();
        var expiryValue = data.get("expiry").asInt();
        return new TokenClaims(subjectValue, clientIdValue, scopeValue.split(" "), expiryValue);
    }

    public TokenClaims(final String subject, final String clientId, final String[] scopes, final int expiry) {

        this.subject = subject;
        this.clientId = clientId;
        this.scopes = scopes;
        this.expiry = expiry;
    }

    public ObjectNode exportData() {

        var mapper = new ObjectMapper();
        var data = mapper.createObjectNode();
        data.put("subject", this.subject);
        data.put("clientId", this.clientId);
        data.put("scopes", String.join(" ", this.scopes));
        data.put("expiry", this.expiry);
        return data;
    }
}
