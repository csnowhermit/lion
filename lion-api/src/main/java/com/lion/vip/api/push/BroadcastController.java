package com.lion.vip.api.push;

import java.util.List;

/**
 * 广播Controller
 */
public interface BroadcastController {

    String taskId();

    int qps();

    void updateQps(int qps);

    boolean isDone();

    int sendCount();

    void cancel();

    boolean isCancelled();

    int incSendCount(int count);

    void success(String... userIds);

    List<String> successUserIds();
}
