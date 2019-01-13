package com.mycompany.api.basicapi.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/*
 * API claims used for authorization
 */
public class ApiClaims {

    // The immutable user id from the access token, which may exist in the API's database
    @Getter
    private String userId;

    // The calling application's client id can potentially be used for authorization
    @Getter
    private String callingApplicationId;

    // OAuth scopes can represent high level areas of the business
    @Getter
    private Set<String> scopes;

    @Getter
    @Setter
    private UserInfoClaims userInfo;

    @Getter
    @Setter
    private Integer[] userCompanyIds;

    /*
     * Initialize from token details we are interested in
     */
    public ApiClaims(String userId, String callingApplicationId, String scope) {
        this.userId = userId;
        this.callingApplicationId = callingApplicationId;
        this.scopes = Set.of(scope.split(" "));
    }
}
