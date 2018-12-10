package com.lion.vip.core.handler;

import com.lion.vip.api.connection.Connection;
import com.lion.vip.api.message.MessageHandler;
import com.lion.vip.api.protocol.Packet;
import com.lion.vip.tools.log.Logs;

/**
 * 心跳处理类
 */
public class HeartBeatHandler implements MessageHandler {

    @Override
    public void handle(Packet packet, Connection connection) {
        connection.send(packet);//ping -> pong
        Logs.HB.info("ping -> pong, {}", connection);
    }
}
