package com.mycompany.api.basicapi.plumbing.threading;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.context.request.RequestContextHolder;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/*
 * Ensure that request scoped items are available during async completion
 * https://stackoverflow.com/questions/23732089/how-to-enable-request-scope-in-async-task-executor/33337838#33337838
 */
public class AsyncRequestThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        System.out.println("***** USING executor submit callable");
        return super.submit(new AsyncRequestCallable(task, RequestContextHolder.currentRequestAttributes()));
    }

    @Override
    public Future<?> submit(Runnable task) {
        System.out.println("***** USING executor submit runnable");
        return super.submit(task);
    }

    @Override
    public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
        System.out.println("***** USING executor submitListenable callable");
        return super.submitListenable(new AsyncRequestCallable(task, RequestContextHolder.currentRequestAttributes()));
    }

    @Override
    public ListenableFuture<?> submitListenable(Runnable task) {
        System.out.println("***** USING executor submitListenable runnable");
        return super.submitListenable(task);
    }

    @Override
    public void execute(Runnable task) {
        System.out.println("***** USING executor execute runnable");
        super.execute(task);
    }

    @Override
    public void execute(Runnable task, long startTimeout) {
        System.out.println("***** USING executor execute runnable + timeout");
        super.execute(task, startTimeout);
    }
}