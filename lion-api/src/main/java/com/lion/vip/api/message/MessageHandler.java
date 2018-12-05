package com.lion.vip.api.message;

import com.lion.vip.api.connection.Connection;
import com.lion.vip.api.protocol.Packet;

/**
 * 对报文的处理接口
 */
public interface MessageHandler {
    void handle(Packet packet, Connection connection);
}
