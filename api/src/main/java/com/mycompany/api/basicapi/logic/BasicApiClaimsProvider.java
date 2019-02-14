package com.mycompany.api.basicapi.logic;

import com.mycompany.api.basicapi.entities.BasicApiClaims;
import com.mycompany.api.basicapi.plumbing.oauth.CustomClaimsProvider;

/*
 * Extend our base class to provide custom claims
 */
public class BasicApiClaimsProvider extends CustomClaimsProvider<BasicApiClaims> {
}
