package com.lion.vip.core;

import com.lion.vip.api.push.PushException;
import com.lion.vip.api.spi.Spi;
import com.lion.vip.common.CommonExecutorFactory;
import com.lion.vip.tools.config.CC;
import com.lion.vip.tools.log.Logs;
import com.lion.vip.tools.thread.NamedPoolThreadFactory;
import com.lion.vip.tools.thread.pool.ThreadPoolConfig;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.lion.vip.tools.config.CC.lion.thread.pool.ack_timer;
import static com.lion.vip.tools.config.CC.lion.thread.pool.push_task;
import static com.lion.vip.tools.thread.ThreadNames.T_ARK_REQ_TIMER;
import static com.lion.vip.tools.thread.ThreadNames.T_MQ;
import static com.lion.vip.tools.thread.ThreadNames.T_PUSH_CENTER_TIMER;

/**
 * 此线程池可伸缩，空闲线程一段时间后回收，新请求重新创建新线程
 */
@Spi(order = 1)
public class ServerExecutorFactory extends CommonExecutorFactory {

    @Override
    public Executor get(String name) {
        final ThreadPoolConfig config;
        switch (name) {
            case MQ:
                config = ThreadPoolConfig.build(T_MQ)
                        .setCorePoolSize(CC.lion.thread.pool.mq.min)
                        .setMaxPoolSize(CC.lion.thread.pool.mq.max)
                        .setKeepAliveSeconds(TimeUnit.SECONDS.toSeconds(10))
                        .setQueueCapacity(CC.lion.thread.pool.mq.queue_size)
                        .setRejectedPolicy(ThreadPoolConfig.REJECTED_POLICY_CALLER_RUNS);
                break;
            case PUSH_TASK:
                return new ScheduledThreadPoolExecutor(push_task, new NamedPoolThreadFactory(T_PUSH_CENTER_TIMER),
                        (r, e) -> {
                            throw new PushException("one push task was rejected. task=" + r);
                        });
            case ACK_TIMER:
                ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(ack_timer,
                        new NamedPoolThreadFactory(T_ARK_REQ_TIMER),
                        (r, e) -> {
                            Logs.PUSH.error("one ack context was rejected, context=" + r);
                        });
                executor.setRemoveOnCancelPolicy(true);
                return executor;
            default:
                return super.get(name);
        }
        return get(config);
    }
}
