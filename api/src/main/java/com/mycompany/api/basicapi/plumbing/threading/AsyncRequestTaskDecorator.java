package com.mycompany.api.basicapi.plumbing.threading;

import org.springframework.core.task.TaskDecorator;

/*
 * See if I can get this to be called
 */
public class AsyncRequestTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {

        // Right now: Web thread context !
        // (Grab the current thread MDC data)
        // Map<String, String> contextMap = MDC.getCopyOfContextMap();

        System.out.println("*** DECORATOR: In web thread context");

        return () -> {
            try {
                // Right now: @Async thread context !
                // (Restore the Web thread context's MDC data)
                //MDC.setContextMap(contextMap);

                System.out.println("*** DECORATOR: Async thread context");
                runnable.run();

            } finally {

                System.out.println("*** DECORATOR: Clearing thread context");
                //MDC.clear();
            }
        };
    }
}