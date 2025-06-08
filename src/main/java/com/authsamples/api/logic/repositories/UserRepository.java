package com.authsamples.api.logic.repositories;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.authsamples.api.plumbing.claims.ExtraValues;

/*
 * A repository that returns hard coded data, whereas a real implementation would use a database lookup
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public final class UserRepository {

    /*
     * Receive the manager ID in the access token, as a useful API identity, then look up extra authorization values
     */
    public ExtraValues getUserInfoForManagerId(final String managerId) {

        if (managerId.equals("20116")) {

            // These values are used for the guestadmin@example.com user account
            return new ExtraValues("Global Manager", new String[]{"Europe", "USA", "Asia"});

        } else if (managerId.equals("10345")) {

            // These values are used for the guestuser@example.com user account
            return new ExtraValues("Regional Manager", new String[]{"USA"});

        } else {

            // Use empty values for unrecognized users
            return new ExtraValues();
        }
    }
}
