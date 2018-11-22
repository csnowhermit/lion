/**
 * FileName: DefaultExecutor
 * Author:   Ren Xiaotian
 * Date:     2018/11/22 16:39
 */

package com.lion.vip.tools.thread.pool;

import java.util.concurrent.*;

public final class DefaultExecutor extends ThreadPoolExecutor {

    public DefaultExecutor(int corePoolSize, int maximumPoolSize,
                           long keepAliveTime, TimeUnit unit,
                           BlockingQueue<Runnable> workQueue,
                           ThreadFactory threadFactory,
                           RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

}
