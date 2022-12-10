package com.mycompany.sample.plumbing.oauth;

import java.util.Arrays;
import org.springframework.http.HttpStatus;
import com.mycompany.sample.plumbing.errors.ErrorCodes;
import com.mycompany.sample.plumbing.errors.ErrorFactory;

/*
 * A utility method to enforce scopes
 */
public final class ScopeVerifier {

    private ScopeVerifier() {
    }

    public static void enforce(final String[] scopes, final String requiredScope) {

        var foundScope = Arrays.stream(scopes).filter(s -> s.contains(requiredScope)).findFirst();
        if (!foundScope.isPresent()) {
            ScopeVerifier.deny();
        }
    }

    /*
     * Return the 403 forbidden error
     */
    public static void deny() {

        throw ErrorFactory.createClientError(
                HttpStatus.FORBIDDEN,
                ErrorCodes.INSUFFICIENT_SCOPE,
                "Access to this API endpoint is forbidden");
    }
}
