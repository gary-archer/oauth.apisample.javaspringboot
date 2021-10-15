package com.mycompany.sample.plumbing.claims;

import java.util.Arrays;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.sample.plumbing.errors.ErrorCodes;
import com.mycompany.sample.plumbing.errors.ErrorFactory;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.Getter;

/*
 * Claims included in the JWT
 */
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class BaseClaims {

    @Getter
    private final String subject;

    @Getter
    private final String[] scopes;

    @Getter
    private final int expiry;

    /*
     * Called when claims are deserialized during claims caching
     */
    public static BaseClaims importData(final JsonNode data) {

        var subjectValue = data.get("sub").asText();
        var scopeValue = data.get("scope").asText();
        var expiryValue = data.get("exp").asInt();
        return new BaseClaims(subjectValue, scopeValue.split(" "), expiryValue);
    }

    /*
     * Receive a claims principal after processing the access token
     */
    public BaseClaims(final JWTClaimsSet claimsSet) {

        this.subject = ClaimParser.getStringClaim(claimsSet, "sub");
        this.scopes = ClaimParser.getStringClaim(claimsSet, "scope").split(" ");
        this.expiry = (int) claimsSet.getExpirationTime().toInstant().getEpochSecond();
    }

    /*
     * Receive individual claims when getting claims from the cache
     */
    public BaseClaims(final String subject, final String[] scopes, final int expiry) {

        this.subject = subject;
        this.scopes = scopes;
        this.expiry = expiry;
    }

    /*
     * Called when claims are serialized during claims caching
     */
    public ObjectNode exportData() {

        var mapper = new ObjectMapper();
        var data = mapper.createObjectNode();
        data.put("sub", this.subject);
        data.put("scope", String.join(" ", this.scopes));
        data.put("exp", this.expiry);
        return data;
    }

    /*
     * Verify that we are allowed to access this type of data, via the scopes from the token
     */
    public void verifyScope(final String scope) {

        var found = Arrays.stream(this.scopes).filter(s -> s.contains(scope)).findFirst();
        if (found.isEmpty()) {
            throw ErrorFactory.createClientError(
                    HttpStatus.FORBIDDEN,
                    ErrorCodes.INSUFFICIENT_SCOPE,
                    "Access token does not have a valid scope for this API");
        }
    }
}
