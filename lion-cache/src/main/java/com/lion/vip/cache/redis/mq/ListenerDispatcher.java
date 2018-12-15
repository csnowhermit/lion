package com.lion.vip.cache.redis.mq;

import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import com.lion.vip.api.LionContext;
import com.lion.vip.api.spi.common.MQClient;
import com.lion.vip.api.spi.common.MQMessageReceiver;
import com.lion.vip.monitor.service.MonitorService;
import com.lion.vip.tools.log.Logs;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

public final class ListenerDispatcher implements MQClient {
    private final Map<String, List<MQMessageReceiver>> subscribes = Maps.newTreeMap();
    private final Subscriber subscriber;

    private Executor executor;

    public ListenerDispatcher(Subscriber subscriber) {
        this.subscriber = subscriber;
    }

    @Override
    public void init(LionContext lionContext) {
        executor = ((MonitorService) lionContext.getMonitor()).getThreadPoolManager().getRedisExecutor();
    }

    public void onMessage(String channel, String message) {
        List<MQMessageReceiver> mqMessageReceiverList = subscriber.get(channel);
        if (mqMessageReceiverList == null) {
            Logs.CACHE.info("cannot find listener:{}, {}", channel, message);
            return;
        }

        for (MQMessageReceiver receiver : mqMessageReceiverList) {
            executor.execute(() -> receiver.receive(channel, message));
        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public void subscribe(String topic, MQMessageReceiver mqMessageReceiver) {

    }

    @Override
    public void publish(String topic, Object message) {

    }
}
