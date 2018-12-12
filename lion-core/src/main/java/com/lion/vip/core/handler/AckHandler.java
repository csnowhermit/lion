package com.lion.vip.core.handler;

import com.lion.vip.api.connection.Connection;
import com.lion.vip.api.protocol.Packet;
import com.lion.vip.common.handler.BaseMessageHandler;
import com.lion.vip.common.message.AckMessage;
import com.lion.vip.core.LionServer;
import com.lion.vip.core.ack.AckTask;
import com.lion.vip.core.ack.AckTaskQueue;
import com.lion.vip.tools.log.Logs;

/**
 * 确认消息的处理类
 */
public class AckHandler extends BaseMessageHandler<AckMessage> {
    private final AckTaskQueue ackTaskQueue;

    public AckHandler(LionServer lionServer) {
        this.ackTaskQueue = lionServer.getPushCenter().getAckTaskQueue();
    }

    @Override
    public AckMessage decode(Packet packet, Connection connection) {
        return new AckMessage(packet, connection);
    }

    @Override
    public void handle(AckMessage message) {
        AckTask ackTask = ackTaskQueue.getAndRemove(message.getSessionId());

        if (ackTask == null) {    //ack超时了
            Logs.PUSH.warn("receive client ack, but task timeout message={}", message);
            return;
        }

        ackTask.onResponse();    //成功收到客户端的ACK响应
    }
}
