package com.mycompany.sample.plumbing.claims;

import java.util.Arrays;
import org.springframework.http.HttpStatus;
import com.mycompany.sample.plumbing.errors.ErrorCodes;
import com.mycompany.sample.plumbing.errors.ErrorFactory;
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
     * Receive individual claims when getting claims from the cache
     */
    public BaseClaims(final String subject, final String[] scopes, final int expiry) {

        this.subject = subject;
        this.scopes = scopes;
        this.expiry = expiry;
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
