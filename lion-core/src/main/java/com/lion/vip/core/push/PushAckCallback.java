package com.lion.vip.core.push;

import com.lion.vip.api.spi.push.IPushMessage;
import com.lion.vip.core.ack.AckCallback;
import com.lion.vip.core.ack.AckTask;
import com.lion.vip.tools.common.TimeLine;
import com.lion.vip.tools.log.Logs;

/**
 * 对推送回应消息的回调
 */
public class PushAckCallback implements AckCallback {

    private final IPushMessage message;
    private final TimeLine timeLine;
    private final PushCenter pushCenter;

    public PushAckCallback(IPushMessage message, TimeLine timeLine, PushCenter pushCenter) {
        this.message = message;
        this.timeLine = timeLine;
        this.pushCenter = pushCenter;
    }


    @Override
    public void onSuccess(AckTask ackTask) {
        pushCenter.getPushListener().onAckSuccess(message, timeLine.successEnd().getTimePoints());
        Logs.PUSH.info("[SingleUserPush] client ack success, timeLine={}, task={}", timeLine, ackTask);
    }

    @Override
    public void onTimeout(AckTask ackTask) {
        pushCenter.getPushListener().onAckSuccess(message, timeLine.failureEnd().getTimePoints());
        Logs.PUSH.info("[SingleUserPush] client ack failure, timeLine={}, task={}", timeLine, ackTask);
    }
}
