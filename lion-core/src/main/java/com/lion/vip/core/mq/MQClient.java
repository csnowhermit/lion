package com.lion.vip.core.mq;

import java.util.Collection;
import java.util.Collections;

/**
 * 消息队列客户端
 */
public final class MQClient {

    public void init() {
    }

    /**
     * 消息队列订阅者
     *
     * @param topic
     * @param receiver
     */
    public void subscribe(String topic, MQMessageReceiver receiver) {

    }

    /**
     * 消息队列消息发布者
     *
     * @param topic
     * @param mqPushMessage
     */
    public void publish(String topic, MQPushMessage mqPushMessage) {

    }

    /**
     * 批量从指定topic拉取消息
     *
     * @param topic
     * @return
     */
    public Collection<MQPushMessage> take(String topic) {
        return Collections.emptyList();
    }

}
