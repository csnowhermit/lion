package com.lion.vip.client.gateway.handler;

import com.lion.vip.api.connection.Connection;
import com.lion.vip.api.protocol.Command;
import com.lion.vip.api.protocol.Packet;
import com.lion.vip.client.LionClient;
import com.lion.vip.client.push.PushRequest;
import com.lion.vip.client.push.PushRequestBus;
import com.lion.vip.common.handler.BaseMessageHandler;
import com.lion.vip.common.message.ErrorMessage;
import com.lion.vip.tools.log.Logs;

import static com.lion.vip.common.ErrorCode.OFFLINE;
import static com.lion.vip.common.ErrorCode.PUSH_CLIENT_FAILURE;
import static com.lion.vip.common.ErrorCode.ROUTER_CHANGE;

public final class GatewayErrorHandler extends BaseMessageHandler<ErrorMessage> {

    private final PushRequestBus pushRequestBus;

    public GatewayErrorHandler(LionClient lionClient) {
        this.pushRequestBus = lionClient.getPushRequestBus();
    }

    @Override
    public ErrorMessage decode(Packet packet, Connection connection) {
        return new ErrorMessage(packet, connection);
    }

    @Override
    public void handle(ErrorMessage message) {
        if (message.cmd == Command.GATEWAY_PUSH.cmd) {
            PushRequest request = pushRequestBus.getAndRemove(message.getSessionId());
            if (request == null) {
                Logs.PUSH.warn("receive a gateway response, but request has timeout. message={}", message);
                return;
            }

            Logs.PUSH.warn("receive an error gateway response, message={}", message);
            if (message.code == OFFLINE.errorCode) {//用户离线
                request.onOffline();
            } else if (message.code == PUSH_CLIENT_FAILURE.errorCode) {//下发到客户端失败
                request.onFailure();
            } else if (message.code == ROUTER_CHANGE.errorCode) {//用户路由信息更改
                request.onRedirect();
            }
        }
    }
}
