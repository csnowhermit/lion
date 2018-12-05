
package com.lion.vip.common.handler;

import com.lion.vip.api.connection.Connection;
import com.lion.vip.api.protocol.Packet;

public class OkMessageHandler extends BaseMessageHandler<OkMessage> {
    @Override
    public OkMessage decode(Packet packet, Connection connection) {
        return new OkMessage(packet, connection);
    }

    @Override
    public void handle(OkMessage message) {

    }
}
