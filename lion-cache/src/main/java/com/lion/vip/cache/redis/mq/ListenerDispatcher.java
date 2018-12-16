package com.lion.vip.cache.redis.mq;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lion.vip.api.LionContext;
import com.lion.vip.api.spi.common.MQClient;
import com.lion.vip.api.spi.common.MQMessageReceiver;
import com.lion.vip.cache.redis.manager.RedisManager;
import com.lion.vip.monitor.service.MonitorService;
import com.lion.vip.tools.log.Logs;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

public final class ListenerDispatcher implements MQClient {
    private final Map<String, List<MQMessageReceiver>> subscribeMap = Maps.newTreeMap();
    private final Subscriber subscriber;

    private Executor executor;

    public ListenerDispatcher() {
        this.subscriber = new Subscriber(this);
    }

    public Subscriber getSubscriber() {
        return subscriber;
    }

    @Override
    public void init(LionContext lionContext) {
        executor = ((MonitorService) lionContext.getMonitor()).getThreadPoolManager().getRedisExecutor();
    }

    public void onMessage(String channel, String message) {
        List<MQMessageReceiver> mqMessageReceiverList = subscribeMap.get(channel);
        if (mqMessageReceiverList == null) {
            Logs.CACHE.info("cannot find listener:{}, {}", channel, message);
            return;
        }

        for (MQMessageReceiver receiver : mqMessageReceiverList) {
            executor.execute(() -> receiver.receive(channel, message));
        }
    }

    @Override
    public void subscribe(String channel, MQMessageReceiver mqMessageReceiver) {
        subscribeMap.computeIfAbsent(channel, k -> Lists.newArrayList()).add(mqMessageReceiver);
        RedisManager.I.subscribe(subscriber, channel);
    }

    @Override
    public void publish(String topic, Object message) {
        RedisManager.I.publish(topic, message);
    }
}
