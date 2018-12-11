package com.lion.vip.core.ack;

import java.util.concurrent.Future;

public class AckTask implements Runnable {
    private final int ackMessageId;
    private AckTaskQueue ackTaskQueue;
    private AckCallback callback;
    private Future<?> timeoutFuture;

    public AckTask(int ackMessageId) {
        this.ackMessageId = ackMessageId;
    }

    public static AckTask from(int ackMessageId) {
        return new AckTask(ackMessageId);
    }

    public int getAckMessageId() {
        return ackMessageId;
    }

    public AckTaskQueue getAckTaskQueue() {
        return ackTaskQueue;
    }

    public AckTask setAckTaskQueue(AckTaskQueue ackTaskQueue) {
        this.ackTaskQueue = ackTaskQueue;
        return this;
    }

    public AckCallback getCallback() {
        return callback;
    }

    public AckTask setCallback(AckCallback callback) {
        this.callback = callback;
        return this;
    }

    public Future<?> getTimeoutFuture() {
        return timeoutFuture;
    }

    public AckTask setFuture(Future<?> timeoutFuture) {
        this.timeoutFuture = timeoutFuture;
        return this;
    }

    public void onResponse(){
        if (tryDone()){
            callback.onSuccess(this);
            callback = null;
        }
    }

    public void onTimeout(){
        AckTask ackTask = ackTaskQueue.getAndRemove(ackMessageId);
        if (ackTask != null && tryDone()){
            callback.onTimeout(this);
            callback = null;
        }
    }

    private boolean tryDone() {
        return timeoutFuture.cancel(true);
    }

    @Override
    public String toString() {
        return "AckTask{" +
                "ackMessageId=" + ackMessageId +
                '}';
    }

    @Override
    public void run() {
        onTimeout();
    }
}
