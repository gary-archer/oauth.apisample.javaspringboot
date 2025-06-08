package com.authsamples.api.plumbing.claims;

import lombok.Getter;

/*
 * Represents extra authorization values not received in access tokens
 */
public final class ExtraValues {

    @Getter
    private final String title;

    @Getter
    private final String[] regions;

    /*
     * A default constructor for Jackson deserialization
     */
    public ExtraValues() {
        title = "";
        regions = new String[0];
    }

    /*
     * Construct with values from the API's own data
     */
    public ExtraValues(final String title, final String[] regions) {
        this.title = title;
        this.regions = regions;
    }
}
