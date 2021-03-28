package com.mycompany.sample.logic.entities;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.mycompany.sample.plumbing.utilities.UnderscorePropertyNamingStrategy;
import lombok.Getter;
import lombok.Setter;

/*
 * OAuth user info for returning to clients for display
 */
@JsonNaming(UnderscorePropertyNamingStrategy.class)
public class ClientUserInfo {

    @Getter
    @Setter
    private String _givenName;

    @Getter
    @Setter
    private String _familyName;
}
