package com.lion.vip.client.gateway.handler;

import com.lion.vip.api.connection.Connection;
import com.lion.vip.api.protocol.Command;
import com.lion.vip.api.protocol.Packet;
import com.lion.vip.client.LionClient;
import com.lion.vip.client.push.PushRequest;
import com.lion.vip.client.push.PushRequestBus;
import com.lion.vip.common.handler.BaseMessageHandler;
import com.lion.vip.common.message.OKMessage;
import com.lion.vip.common.push.GatewayPushResult;
import com.lion.vip.tools.log.Logs;

public final class GatewayOKHandler extends BaseMessageHandler<OKMessage> {

    private PushRequestBus pushRequestBus;

    public GatewayOKHandler(LionClient lionClient) {
        this.pushRequestBus = lionClient.getPushRequestBus();
    }

    @Override
    public OKMessage decode(Packet packet, Connection connection) {
        return new OKMessage(packet, connection);
    }

    @Override
    public void handle(OKMessage message) {
        if (message.cmd == Command.GATEWAY_PUSH.cmd) {
            PushRequest request = pushRequestBus.getAndRemove(message.getSessionId());
            if (request == null) {
                Logs.PUSH.warn("receive a gateway response, but request has timeout. message={}", message);
                return;
            }
            request.onSuccess(GatewayPushResult.fromJson(message.data));//推送成功
        }
    }
}
