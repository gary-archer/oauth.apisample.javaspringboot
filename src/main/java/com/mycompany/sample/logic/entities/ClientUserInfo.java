package com.mycompany.sample.logic.entities;

import lombok.Getter;
import lombok.Setter;

/*
 * User info from the API's own data, returned to clients for display
 */
public class ClientUserInfo {

    @Getter
    @Setter
    private String title;

    @Getter
    @Setter
    private String[] regions;
}
