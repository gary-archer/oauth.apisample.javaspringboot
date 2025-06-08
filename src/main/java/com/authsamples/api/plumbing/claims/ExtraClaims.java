package com.authsamples.api.plumbing.claims;

import lombok.Getter;

/*
 * Represents extra authorization values not received in access tokens
 */
public final class ExtraClaims {

    @Getter
    private final String title;

    @Getter
    private final String[] regions;

    /*
     * A default constructor for Jackson deserialization
     */
    public ExtraClaims() {
        title = "";
        regions = new String[0];
    }

    /*
     * Construct with values from the API's own data
     */
    public ExtraClaims(final String title, final String[] regions) {
        this.title = title;
        this.regions = regions;
    }
}
