package com.lion.vip.api.common;

import java.util.concurrent.Executor;

/**
 * 监控
 */
public interface Monitor {

    void monitor(String name, Thread thread);
    void monitor(String name, Executor executor);
}
