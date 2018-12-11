package com.lion.vip.core.push;

import java.util.concurrent.ScheduledExecutorService;

/**
 * 推送任务
 */
public interface PushTask extends Runnable {
    ScheduledExecutorService getExecutor();
}
