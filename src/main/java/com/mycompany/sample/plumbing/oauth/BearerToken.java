package com.mycompany.sample.plumbing.oauth;

import javax.servlet.http.HttpServletRequest;

/*
 * A simple utility class to read the access token
 */
public final class BearerToken {

    private BearerToken() {
    }

    /*
     * Try to read the access token from the authorization header
     */
    public static String read(final HttpServletRequest request) {

        var header = request.getHeader("Authorization");
        if (header != null) {
            var parts = header.split(" ");
            if (parts.length == 2 && parts[0].equalsIgnoreCase("Bearer")) {
                return parts[1];
            }
        }

        return null;
    }
}
