package com.lion.vip.core.handler;

import com.lion.vip.api.connection.Connection;
import com.lion.vip.api.protocol.Packet;
import com.lion.vip.common.handler.BaseMessageHandler;
import com.lion.vip.common.message.gateway.GatewayPushMessage;
import com.lion.vip.core.push.PushCenter;

/**
 * 网关消息的处理类
 */
public class GatewayPushHandler extends BaseMessageHandler<GatewayPushMessage> {

    private final PushCenter pushCenter;

    public GatewayPushHandler(PushCenter pushCenter) {
        this.pushCenter = pushCenter;
    }

    @Override
    public GatewayPushMessage decode(Packet packet, Connection connection) {
        return new GatewayPushMessage(packet, connection);
    }

    @Override
    public void handle(GatewayPushMessage message) {
        this.pushCenter.push(message);
    }
}
