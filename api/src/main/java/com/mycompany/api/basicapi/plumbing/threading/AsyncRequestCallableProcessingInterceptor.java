package com.mycompany.api.basicapi.plumbing.threading;

import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;
import java.util.concurrent.Callable;

/*
 * See if I can intercept and maintain request context here
 */
public class AsyncRequestCallableProcessingInterceptor implements CallableProcessingInterceptor {
    @Override
    public <T> void beforeConcurrentHandling (
            NativeWebRequest request,
            Callable<T> task) throws Exception {
        System.out.println("*** callableInterceptor#beforeConcurrentHandling called. " +
                "Thread: " + Thread.currentThread().getName());
    }

    @Override
    public <T> void preProcess (
            NativeWebRequest request,
            Callable<T> task) throws Exception {

        System.out.println("*** callableInterceptor#preProcess called. "+
                " Thread: " + Thread.currentThread().getName());
    }

    @Override
    public <T> void postProcess (NativeWebRequest request,
                                 Callable<T> task,
                                 Object concurrentResult) throws Exception {
        System.out.println("*** callableInterceptor#postProcess called. "+
                " Thread: " + Thread.currentThread().getName());
    }

    @Override
    public <T> Object handleTimeout (NativeWebRequest request,
                                     Callable<T> task) throws Exception {

        System.out.println("*** callableInterceptor#handleTimeout called."+
                " Thread: " + Thread.currentThread().getName());

        return RESULT_NONE;
    }

    @Override
    public <T> void afterCompletion (NativeWebRequest request,
                                     Callable<T> task) throws Exception {
        System.out.println("*** callableInterceptor#afterCompletion called."+
                " Thread: " + Thread.currentThread().getName());
    }
}
