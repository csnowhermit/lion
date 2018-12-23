package com.lion.vip.client;

import com.lion.vip.api.spi.Spi;
import com.lion.vip.common.CommonExecutorFactory;
import com.lion.vip.tools.log.Logs;
import com.lion.vip.tools.thread.NamedPoolThreadFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static com.lion.vip.tools.config.CC.lion.thread.pool.ack_timer;
import static com.lion.vip.tools.config.CC.lion.thread.pool.push_client;
import static com.lion.vip.tools.thread.ThreadNames.T_ARK_REQ_TIMER;
import static com.lion.vip.tools.thread.ThreadNames.T_PUSH_CLIENT_TIMER;

/**
 * 客户端线程池处理工厂
 */
@Spi(order = 1)
public final class ClientExecutorFactory extends CommonExecutorFactory {

    @Override
    public Executor get(String name) {
        switch (name) {
            case PUSH_CLIENT: {
                ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(push_client,
                        new NamedPoolThreadFactory(T_PUSH_CLIENT_TIMER),
                        (r, e) -> r.run());

                executor.setRemoveOnCancelPolicy(true);
                return executor;
            }
            case ACK_TIMER: {
                ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(ack_timer,
                        new NamedPoolThreadFactory(T_ARK_REQ_TIMER),
                        (r, e) -> Logs.PUSH.error("one ack context was rejected, context=" + r));

                executor.setRemoveOnCancelPolicy(true);
                return executor;
            }
            default:
                return super.get(name);
        }
    }
}
