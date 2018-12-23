package com.lion.vip.client.push;

import com.lion.vip.api.push.PushSender;
import com.lion.vip.api.spi.client.PusherFactory;

/**
 * 消息推送客户端 工厂类
 */
public class PushClientFactory implements PusherFactory {

    private volatile PushClient client;

    @Override
    public PushSender get() {
        if (client == null) {
            synchronized (PushClientFactory.class) {
                if (client == null) {
                    client = new PushClient();
                }
            }
        }

        return client;
    }
}
