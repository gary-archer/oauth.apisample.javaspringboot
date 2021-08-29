package com.mycompany.sample.plumbing.claims;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nimbusds.jwt.JWTClaimsSet;
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

    /*
     * Called when claims are deserialized during claims caching
     */
    public static UserInfoClaims importData(final JsonNode data) {

        var givenNameValue = data.get("given_name").asText();
        var familyNameValue = data.get("family_name").asText();
        var emailValue = data.get("email").asText();
        return new UserInfoClaims(givenNameValue, familyNameValue, emailValue);
    }

    /*
     * Receive a claims set
     */
    public UserInfoClaims(final JWTClaimsSet claimsSet) {

        this.givenName = ClaimParser.getStringClaim(claimsSet, "given_name");
        this.familyName = ClaimParser.getStringClaim(claimsSet, "family_name");
        this.email = ClaimParser.getStringClaim(claimsSet, "email");
    }

    /*
     * Receive individual claims values
     */
    public UserInfoClaims(final String givenName, final String familyName, final String email) {

        this.givenName = givenName;
        this.familyName = familyName;
        this.email = email;
    }

    /*
     * Called when claims are serialized during claims caching
     */
    public ObjectNode exportData() {

        var mapper = new ObjectMapper();
        var data = mapper.createObjectNode();
        data.put("given_name", this.givenName);
        data.put("family_name", this.familyName);
        data.put("email", this.email);
        return data;
    }
}
