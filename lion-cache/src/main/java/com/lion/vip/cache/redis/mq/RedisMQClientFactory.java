package com.lion.vip.cache.redis.mq;

import com.lion.vip.api.spi.common.MQClient;
import com.lion.vip.api.spi.common.MQClientFactory;

public final class RedisMQClientFactory implements MQClientFactory {
    private ListenerDispatcher listenerDispatcher = new ListenerDispatcher();

    @Override
    public MQClient get() {
        return listenerDispatcher;
    }
}
