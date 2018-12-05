package com.lion.vip.api.spi.common;

import com.lion.vip.api.spi.SpiLoader;

import java.util.concurrent.Executor;

public interface ExecutorFactory {
    String PUSH_CLIENT = "push-client";
    String PUSH_TASK = "push-task";
    String ACK_TIMER = "ack-timer";
    String EVENT_BUS = "event-bus";
    String MQ = "mq";

    Executor get(String name);

    static ExecutorFactory create() {
        return SpiLoader.load(ExecutorFactory.class);
    }
}
