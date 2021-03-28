package com.mycompany.sample.plumbing.claims;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;

/*
 * Claims included in OAuth user info
 */
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class UserInfoClaims {

    @Getter
    private final String _givenName;

    @Getter
    private final String _familyName;

    @Getter
    private final String _email;

    public static UserInfoClaims importData(final JsonNode data) {

        var givenNameValue = data.get("givenName").asText();
        var familyNameValue = data.get("familyName").asText();
        var emailValue = data.get("email").asText();
        return new UserInfoClaims(givenNameValue, familyNameValue, emailValue);
    }

    public UserInfoClaims(final String givenName, final String familyName, final String email) {

        this._givenName = givenName;
        this._familyName = familyName;
        this._email = email;
    }

    public ObjectNode exportData() {

        var mapper = new ObjectMapper();
        var data = mapper.createObjectNode();
        data.put("givenName", this._givenName);
        data.put("familyName", this._familyName);
        data.put("email", this._email);
        return data;
    }
}
