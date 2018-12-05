package com.lion.vip.api.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 基础服务
 */
public abstract class BaseService implements Service {

    protected final AtomicBoolean started = new AtomicBoolean();    //原子操作类

    @Override
    public boolean isRunning() {
        return started.get();
    }

    @Override
    public void start(Listener listener) {
        tryStart(listener, this::doStart);
    }

    private void tryStart(Listener listener, FunctionEx function) {
        FutureListener futureListener = wrap(listener);    //防止Listener被重复执行
        if (started.compareAndSet(false, true)) {
            try {
                init();
                function.apply(listener);
                futureListener.monitor(this);    //主要用于异步，否则应该防止在 function.apply(listener) 之前
            } catch (Throwable e) {
                listener.onFailure(e);
                throw new ServiceException(e);
            }
        } else {
            if (throwIfStarted()) {
                listener.onFailure(new ServiceException("Service already started"));
            } else {
                listener.onSuccess();
            }
        }
    }

    @Override
    public void stop(Listener listener) {
        tryStop(listener, this::doStop);
    }

    private void tryStop(Listener listener, FunctionEx function) {
        FutureListener futureListener = wrap(listener);
        if (started.compareAndSet(true, false)) {
            try {
                function.apply(listener);
                futureListener.monitor(this);    //主要用于异步，否则应该放在 function.apply(listener) 之前
            } catch (Throwable e) {
                listener.onFailure(e);
                throw new ServiceException(e);
            }
        } else {
            if (throwIfStoped()) {
                listener.onFailure(new ServiceException("Service already stopped"));
            } else {
                listener.onSuccess();
            }
        }
    }

    protected void doStart(Listener listener) throws Throwable {
        listener.onSuccess();
    }

    protected void doStop(Listener listener) throws Throwable {
        listener.onSuccess();
    }

    @Override
    public CompletableFuture<Boolean> start() {
        FutureListener listener = new FutureListener(started);
        start(listener);
        return listener;
    }

    @Override
    public CompletableFuture<Boolean> stop() {
        FutureListener listener = new FutureListener(started);
        stop(listener);
        return listener;
    }

    @Override
    public boolean syncStart() {
        return start().join();
    }

    @Override
    public boolean syncStop() {
        return stop().join();
    }

    @Override
    public void init() {

    }

    /**
     * 控制，当服务启动后，重复调用start()方法，是否抛出 服务已启动 异常
     *
     * @return true：抛出异常
     */
    protected boolean throwIfStarted() {
        return true;
    }

    /**
     * 控制，当服务停止后，重复调用stop()方法，是否抛出 服务已停止 异常
     *
     * @return true：抛出异常
     */
    protected boolean throwIfStoped() {
        return true;
    }

    /**
     * 服务启动停止，超时时间，默认为10s
     *
     * @return
     */
    protected int timeoutMillis() {
        return 1000 * 10;
    }

    protected interface FunctionEx {
        void apply(Listener listener) throws Throwable;
    }

    /**
     * 防止Listener被重复执行
     *
     * @param listener Listener
     * @return FutureListener
     */
    public FutureListener wrap(Listener listener) {
        if (listener == null) {
            return new FutureListener(started);
        }
        if (listener instanceof FutureListener) {
            return (FutureListener) listener;
        } else {
            return new FutureListener(listener, started);
        }
    }

}
