package com.mycompany.sample.plumbing.claims;

import lombok.Getter;

/*
 * Claims included in the JWT
 */
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class BaseClaims {

    @Getter
    private final String subject;

    @Getter
    private final String[] scopes;

    @Getter
    private final int expiry;

    /*
     * Receive individual claims when getting claims from the cache
     */
    public BaseClaims(final String subject, final String[] scopes, final int expiry) {

        this.subject = subject;
        this.scopes = scopes;
        this.expiry = expiry;
    }
}
