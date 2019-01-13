package com.mycompany.api.basicapi.plumbing.threading;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import java.util.concurrent.Callable;

/*
 * A callable to pass request state, including @RequestScope items, to the async completion thread
 */
public class AsyncRequestCallable<T> implements Callable<T> {

    private Callable<T> task;
    private RequestAttributes context;

    public AsyncRequestCallable(Callable<T> task, RequestAttributes context) {
        this.task = task;
        this.context = context;
    }

    @Override
    public T call() throws Exception {
        if (context != null) {
            System.out.println("***** Setting async context");
            RequestContextHolder.setRequestAttributes(context);
        }

        try {
            System.out.println("***** Making async call");
            return task.call();
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }
}