package com.authsamples.api.plumbing.utilities;

import java.util.regex.Pattern;

/*
 * Text validation utilities
 */
public final class TextValidator {

    /*
     * Private constructor
     */
    private TextValidator() {
    }

    /*
     * Sanitize input text such as correlation IDs and reject suspicious input
     */
    public static String sanitize(final String input) {

        var pattern = Pattern.compile("/^[a-zA-Z0-9-]+$/i");
        var matcher = pattern.matcher(input);
        if (matcher.find()) {
            System.out.println("*** PASS ***");
            return input;
        }

        return "";
    }
}
