package com.lion.vip.core.push;

import com.lion.vip.api.service.BaseService;
import com.lion.vip.api.service.Listener;
import com.lion.vip.api.spi.push.IPushMessage;
import com.lion.vip.api.spi.push.MessagePusher;
import com.lion.vip.api.spi.push.PushListener;
import com.lion.vip.api.spi.push.PushListenerFactory;
import com.lion.vip.common.qps.FastFlowControl;
import com.lion.vip.common.qps.FlowControl;
import com.lion.vip.common.qps.GlobalFlowControl;
import com.lion.vip.common.qps.RedisFlowControl;
import com.lion.vip.core.LionServer;
import com.lion.vip.core.ack.AckTaskQueue;
import com.lion.vip.monitor.jmx.MBeanRegistry;
import com.lion.vip.monitor.jmx.mxbean.PushCenterBean;
import com.lion.vip.tools.config.CC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.lion.vip.tools.config.CC.lion.push.flow_control.global.limit;
import static com.lion.vip.tools.config.CC.lion.push.flow_control.global.max;
import static com.lion.vip.tools.config.CC.lion.push.flow_control.global.duration;

/**
 * 推送中心
 */
public class PushCenter extends BaseService implements MessagePusher {
    private final Logger LOGGER = LoggerFactory.getLogger(PushCenter.class);

    private final GlobalFlowControl globalFlowControl = new GlobalFlowControl(
            limit,
            max,
            duration
    );

    private final AtomicLong taskNum = new AtomicLong();
    private final AckTaskQueue ackTaskQueue;
    private final LionServer lionServer;
    private PushListener<IPushMessage> pushListener;
    private PushTaskExecutor executor;

    public PushCenter(LionServer lionServer) {
        this.lionServer = lionServer;
        this.ackTaskQueue = new AckTaskQueue(lionServer);
    }

    @Override
    public void push(IPushMessage message) {
        if (message.isBroadcast()) {
            FlowControl flowControl = (message.getTaskId() == null)
                    ? new FastFlowControl(limit, max, duration)
                    : new RedisFlowControl(message.getTaskId(), max);
            addTask(new BroadCastPushTask(lionServer, message, flowControl));    //广播推送任务
        } else {
            addTask(new SingleUserPushTask(lionServer, message, globalFlowControl));   //单用户推送任务
        }
    }

    public void addTask(PushTask task) {
        executor.addTask(task);
        LOGGER.debug("add new task to push center, count={}, task={}", taskNum.incrementAndGet(), task);
    }

    public void delayTask(long delay, PushTask task) {
        executor.delayTask(delay, task);
        LOGGER.debug("delay task to push center, count={}, task={}", taskNum.incrementAndGet(), task);
    }

    @Override
    protected void doStart(Listener listener) throws Throwable {
        this.pushListener = PushListenerFactory.create();
        this.pushListener.init(lionServer);

        if (CC.lion.net.udpGateway() || CC.lion.thread.pool.push_task > 0) {
            executor = new CustomJDKExecutor(lionServer.getMonitor().getThreadPoolManager().getPushTaskTimer());
        } else {
            executor = new NettyEventLoopExecutor();
        }

        MBeanRegistry.getInstance().register(new PushCenterBean(taskNum), null);
        ackTaskQueue.start();

        LOGGER.info("Push Center start success");
        listener.onSuccess();
    }

    @Override
    protected void doStop(Listener listener) throws Throwable {
        executor.shutdown();
        ackTaskQueue.stop();
        LOGGER.info("Push Center stop success");
        listener.onSuccess();
    }

    public PushListener<IPushMessage> getPushListener() {
        return pushListener;
    }

    public AckTaskQueue getAckTaskQueue() {
        return ackTaskQueue;
    }

    /**
     * TCP 模式直接使用GatewayServer work 线程池
     */
    private static class NettyEventLoopExecutor implements PushTaskExecutor {

        @Override
        public void shutdown() {
        }

        @Override
        public void addTask(PushTask task) {
            task.getExecutor().execute(task);
        }

        @Override
        public void delayTask(long delay, PushTask task) {
            task.getExecutor().schedule(task, delay, TimeUnit.NANOSECONDS);
        }
    }

    /**
     * UDP 模式使用自定义线程池
     */
    private static class CustomJDKExecutor implements PushTaskExecutor {
        private final ScheduledExecutorService executorService;

        private CustomJDKExecutor(ScheduledExecutorService executorService) {
            this.executorService = executorService;
        }

        @Override
        public void shutdown() {
            executorService.shutdown();
        }

        @Override
        public void addTask(PushTask task) {
            executorService.execute(task);
        }

        @Override
        public void delayTask(long delay, PushTask task) {
            executorService.schedule(task, delay, TimeUnit.NANOSECONDS);
        }
    }

    private interface PushTaskExecutor {

        void shutdown();

        void addTask(PushTask task);

        void delayTask(long delay, PushTask task);
    }


}
