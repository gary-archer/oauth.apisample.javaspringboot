package com.mycompany.sample.logic.repositories;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.mycompany.sample.logic.claims.SampleExtraClaims;

/*
 * A repository that returns hard coded data, whereas a real implementation would use a database lookup
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public final class UserRepository {

    /*
     * Receive the manager ID in the access token, as a useful API identity, then look up extra claims
     * This is the preferred model, since it locks down the access token and provides the most useful API user identity
     */
    public SampleExtraClaims getClaimsForManagerId(final String managerId) {

        if (managerId.equals("20116")) {

            // These claims are used for the guestadmin@mycompany.com user account
            return new SampleExtraClaims("Global Manager", new String[]{"Europe", "USA", "Asia"});

        } else if (managerId.equals("10345")) {

            // These claims are used for the guestuser@mycompany.com user account
            return new SampleExtraClaims("Regional Manager", new String[]{"USA"});

        } else {

            // Use empty claims for unrecognized users
            return new SampleExtraClaims("", new String[]{});
        }
    }

    /*
     * Receive the subject claim from the access token and look up all other claims
     * This is less optimal, since the token is less locked down and the API must map the subject to other values
     */
    public SampleExtraClaims getClaimsForSubject(final String subject) {

        if (subject.equals("77a97e5b-b748-45e5-bb6f-658e85b2df91")) {

            // These claims are used for the guestadmin@mycompany.com user account
            var claims = new SampleExtraClaims("Global Manager", new String[]{"Europe", "USA", "Asia"});
            claims.addCoreClaims("20116", "admin");
            return claims;

        } else if (subject.equals("a6b404b1-98af-41a2-8e7f-e4061dc0bf86")) {

            // These claims are used for the guestuser@mycompany.com user account
            var claims = new SampleExtraClaims("Regional Manager", new String[]{"USA"});
            claims.addCoreClaims("10345", "user");
            return claims;

        } else {

            // Use empty claims for unrecognized users
            var claims = new SampleExtraClaims("", new String[]{});
            claims.addCoreClaims("", "");
            return claims;
        }
    }
}
