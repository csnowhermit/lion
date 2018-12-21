package com.lion.vip.core.mq;

import com.lion.vip.core.push.PushCenter;
import com.lion.vip.tools.Utils;
import com.lion.vip.tools.config.ConfigTools;

import java.util.Collection;

public final class MQMessageReceiver {
    private final static String TOPIC = "/lion/push" + ConfigTools.getLocalIp();

    private final MQClient mqClient;
    private PushCenter pushCenter;

    public MQMessageReceiver(MQClient mqClient, PushCenter pushCenter) {
        this.mqClient = mqClient;
        this.pushCenter = pushCenter;
    }

    public static void subscribe(MQClient mqClient, PushCenter pushCenter) {
        MQMessageReceiver receiver = new MQMessageReceiver(mqClient, pushCenter);
        mqClient.subscribe(TOPIC, receiver);
        receiver.fetchFormMQ();
    }

    private void fetchFormMQ() {
        Utils.newThread("mq-push", this::dispatch);
    }

    private void dispatch() {
        try {
            while (true) {
                Collection<MQPushMessage> messages = mqClient.take(TOPIC);
                if (messages == null || messages.isEmpty()) {
                    Thread.sleep(100);
                    continue;
                }
            }
        } catch (InterruptedException e) {
            this.dispatch();
        }
    }

    public void onMessage(MQPushMessage mqPushMessage) {
        pushCenter.push(mqPushMessage);
    }

}
