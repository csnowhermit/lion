package com.lion.vip.api.spi.push;

/**
 * 消息推送
 */
public interface MessagePusher {

    void push(IPushMessage message);
}
