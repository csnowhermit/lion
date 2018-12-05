package com.lion.vip.api.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class FutureListener extends CompletableFuture implements Listener {
    private final Listener listener;
    private final AtomicBoolean started;

    public FutureListener(AtomicBoolean started) {
        this.started = started;
        this.listener = null;
    }

    public FutureListener(Listener listener, AtomicBoolean started) {
        this.listener = listener;
        this.started = started;
    }

    @Override
    public void onSuccess(Object... args) {
        if (isDone()) {    //防止Listener被重复执行
            return;
        }

        complete(started.get());

        if (listener != null) {
            listener.onSuccess(args);
        }
    }

    @Override
    public void onFailure(Throwable cause) {
        if (isDone()) {    //防止Listener被重复执行
            return;
        }

        completeExceptionally(cause);
        if (listener != null) {
            listener.onFailure(cause);
        }

        throw cause instanceof ServiceException ? (ServiceException) cause : new ServiceException(cause);
    }


    /**
     * 防止服务长时间卡在某个地方，增加超时监控
     *
     * @param service 服务
     */

    public void monitor(BaseService service) {
        if (isDone()) {
            return;
        }

        runAsync(() -> {
            try {
                this.get(service.timeoutMillis(), TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                this.onFailure(new ServiceException(String.format("Service %s monitor timeout", service.getClass().getSimpleName())));
            }

        });
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }
}
