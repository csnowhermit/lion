package com.lion.vip.core.ack;

import com.lion.vip.api.common.Monitor;
import com.lion.vip.api.service.BaseService;
import com.lion.vip.api.service.Listener;
import com.lion.vip.core.LionServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class AckTaskQueue extends BaseService {
    private static final int DEFAULT_TIMEOUT = 3000;    //默认超时时间

    private final Logger LOGGER = LoggerFactory.getLogger(AckTaskQueue.class);

    private final ConcurrentMap<Integer, AckTask> queue = new ConcurrentHashMap<>();    //ack回应集合
    private ScheduledExecutorService scheduledExecutor;
    private LionServer lionServer;

    public AckTaskQueue(LionServer lionServer) {
        this.lionServer = lionServer;
    }

    public void add(AckTask ackTask, int timeout) {
        queue.put(ackTask.getAckMessageId(), ackTask);
        ackTask.setAckTaskQueue(this);
        ackTask.setFuture(scheduledExecutor.schedule(ackTask,
                timeout > 0 ? timeout : DEFAULT_TIMEOUT,
                TimeUnit.MILLISECONDS));

        LOGGER.debug("one ack task add to queue, ackTask={}, timeout={}", ackTask, timeout);
    }


    public AckTask getAndRemove(int ackMessageId) {
        return queue.remove(ackMessageId);
    }

    @Override
    protected void doStart(Listener listener) throws Throwable {
        scheduledExecutor = lionServer.getMonitor().getThreadPoolManager().getAckTimer();
        super.doStart(listener);
    }

    @Override
    protected void doStop(Listener listener) throws Throwable {
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
        }
        super.doStop(listener);
    }
}
