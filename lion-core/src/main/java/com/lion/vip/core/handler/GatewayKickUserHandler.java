package com.lion.vip.core.handler;

import com.lion.vip.api.connection.Connection;
import com.lion.vip.api.protocol.Packet;
import com.lion.vip.common.handler.BaseMessageHandler;
import com.lion.vip.common.message.gateway.GatewayKickUserMessage;
import com.lion.vip.core.router.RouterCenter;

/**
 * 踢人消息的处理类
 */
public final class GatewayKickUserHandler extends BaseMessageHandler<GatewayKickUserMessage> {

    private final RouterCenter routerCenter;

    public GatewayKickUserHandler(RouterCenter routerCenter) {
        this.routerCenter = routerCenter;
    }

    @Override
    public GatewayKickUserMessage decode(Packet packet, Connection connection) {
        return new GatewayKickUserMessage(packet, connection);
    }

    @Override
    public void handle(GatewayKickUserMessage message) {
        routerCenter.getRouterChangeListener().onReceiveKickRemoteMsg(message);
    }
}
