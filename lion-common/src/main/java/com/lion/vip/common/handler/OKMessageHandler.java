
package com.lion.vip.common.handler;

import com.lion.vip.api.connection.Connection;
import com.lion.vip.api.protocol.Packet;
import com.lion.vip.common.message.OKMessage;

public class OKMessageHandler extends BaseMessageHandler<OKMessage> {
    @Override
    public OKMessage decode(Packet packet, Connection connection) {
        return new OKMessage(packet, connection);
    }

    @Override
    public void handle(OKMessage message) {

    }
}
