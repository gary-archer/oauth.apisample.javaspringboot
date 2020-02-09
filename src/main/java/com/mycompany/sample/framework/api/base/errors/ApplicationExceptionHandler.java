package com.mycompany.sample.framework.api.base.errors;

/*
 * Framework processing can be extended by the consuming API
 */
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class ApplicationExceptionHandler {

    /*
     * Can be overridden to allow the application to translate some types of exception
     */
    public Throwable translate(final Throwable ex) {
        return ex;
    }
}
