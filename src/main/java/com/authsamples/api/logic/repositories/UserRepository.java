package com.authsamples.api.logic.repositories;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.authsamples.api.logic.claims.ExtraClaims;

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
    public ExtraClaims getUserInfoForManagerId(final String managerId) {

        if (managerId.equals("20116")) {

            // These claims are used for the guestadmin@example.com user account
            return new ExtraClaims("Global Manager", new String[]{"Europe", "USA", "Asia"});

        } else if (managerId.equals("10345")) {

            // These claims are used for the guestuser@example.com user account
            return new ExtraClaims("Regional Manager", new String[]{"USA"});

        } else {

            // Use empty claims for unrecognized users
            return new ExtraClaims("", new String[]{});
        }
    }
}
