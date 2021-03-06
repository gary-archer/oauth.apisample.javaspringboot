package com.mycompany.sample.plumbing.claims;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;

/*
 * Claims included in OAuth user info
 */
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class UserInfoClaims {

    @Getter
    private final String givenName;

    @Getter
    private final String familyName;

    @Getter
    private final String email;

    public static UserInfoClaims importData(final ObjectNode data) {

        var givenNameValue = data.get("givenName").asText();
        var familyNameValue = data.get("familyName").asText();
        var emailValue = data.get("email").asText();
        return new UserInfoClaims(givenNameValue, familyNameValue, emailValue);
    }

    public UserInfoClaims(final String givenName, final String familyName, final String email) {

        this.givenName = givenName;
        this.familyName = familyName;
        this.email = email;
    }

    public ObjectNode exportData() {

        var mapper = new ObjectMapper();
        var data = mapper.createObjectNode();
        data.put("givenName", this.givenName);
        data.put("familyName", this.familyName);
        data.put("email", this.email);
        return data;
    }
}
