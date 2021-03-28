package com.mycompany.sample.plumbing.claims;

import java.util.Arrays;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.sample.plumbing.errors.ErrorCodes;
import com.mycompany.sample.plumbing.errors.ErrorFactory;
import lombok.Getter;

/*
 * Claims included in the JWT
 */
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class BaseClaims {

    @Getter
    private final String _subject;

    @Getter
    private final String[] _scopes;

    @Getter
    private final int _expiry;

    /*
     * Read claims from the claims cache
     */
    public static BaseClaims importData(final JsonNode data) {

        var subjectValue = data.get("subject").asText();
        var scopeValue = data.get("scopes").asText();
        var expiryValue = data.get("expiry").asInt();
        return new BaseClaims(subjectValue, scopeValue.split(" "), expiryValue);
    }

    public BaseClaims(final String subject, final String[] scopes, final int expiry) {

        this._subject = subject;
        this._scopes = scopes;
        this._expiry = expiry;
    }

    /*
     * Write data to the claims cache
     */
    public ObjectNode exportData() {

        var mapper = new ObjectMapper();
        var data = mapper.createObjectNode();
        data.put("subject", this._subject);
        data.put("scopes", String.join(" ", this._scopes));
        data.put("expiry", this._expiry);
        return data;
    }

    /*
     * Verify that we are allowed to access this type of data, via the scopes from the token
     */
    public void verifyScope(final String scope) {

        var found = Arrays.stream(this._scopes).filter(s -> s.contains(scope)).findFirst();
        if (found.isEmpty()) {
            throw ErrorFactory.createClientError(
                    HttpStatus.FORBIDDEN,
                    ErrorCodes.INSUFFICIENT_SCOPE,
                    "Access token does not have a valid scope for this API");
        }
    }
}
