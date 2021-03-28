package com.mycompany.sample.plumbing.utilities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;

/*
 * A custom naming strategy when serializing objects with our underscore naming scheme
 */
public final class UnderscorePropertyNamingStrategy extends PropertyNamingStrategy {

    /*
     * Remove the underscore from getter methods
     */
    public String nameForGetterMethod(
            final MapperConfig<?> config,
            final AnnotatedMethod method,
            final String defaultName) {

        if (defaultName.startsWith("_")) {
            return defaultName.substring(1);
        } else {
            return defaultName;
        }
    }

    /*
     * Remove the underscore from setter methods
     */
    public String nameForSetterMethod(
            final MapperConfig<?> config,
            final AnnotatedMethod method,
            final String defaultName) {

        if (defaultName.startsWith("_")) {
            return defaultName.substring(1);
        } else {
            return defaultName;
        }
    }
}
