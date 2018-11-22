/**
 * FileName: PushSender
 * Author:   Ren Xiaotian
 * Date:     2018/11/21 15:17
 */

package com.lion.vip.api.push;

import com.lion.vip.api.LionContext;
import com.lion.vip.api.service.Service;
import com.lion.vip.api.spi.client.PusherFactory;

import java.util.concurrent.FutureTask;

/**
 * 推送发送者
 */
public interface PushSender extends Service {

    /**
     * 创建PushSender实例
     *
     * @return
     */
    static PushSender create() {
        return (PushSender) PusherFactory.create();
    }

    /**
     * push 推送消息
     *
     * @param pushContext
     * @return FutureTask 可用于同步调用
     */
    FutureTask<PushResult> send(PushContext pushContext);

    /**
     * send方法的默认实现，不需要确认
     *
     * @param context
     * @param userId
     * @param pushCallback
     * @return
     */
    default FutureTask<PushResult> send(String context, String userId, PushCallback pushCallback) {
        return send(PushContext.build(context)
                .setUserId(userId)
                .setCallback(pushCallback));
    }

    default FutureTask<PushResult> send(String context, String userId, AckModel ackModel, PushCallback pushCallback) {
        return send(PushContext.build(context)
                .setUserId(userId)
                .setAckModel(ackModel)
                .setCallback(pushCallback));
    }

    default void setLionContext(LionContext lionContext) {
    }

}
