package com.mycompany.sample.framework.api.base.logging;

/*
 * A simple builder class to expose a segregated interface
 */
public final class LoggerFactoryBuilder {

    private LoggerFactoryBuilder() {
    }

    public static LoggerFactory create() {
        return new LoggerFactoryImpl();
    }
}
