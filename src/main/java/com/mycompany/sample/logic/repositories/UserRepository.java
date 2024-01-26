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

        if (subject.equals("d3d64319-1f84-42bb-92cb-5883793c50dc")) {

            // These claims are used for the guestadmin@mycompany.com user account
            var claims = new SampleExtraClaims("Global Manager", new String[]{"Europe", "USA", "Asia"});
            claims.addCoreClaims("20116", "admin");
            return claims;

        } else if (subject.equals("06e3c525-33d1-47ec-97be-03d8affc3726")) {

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
