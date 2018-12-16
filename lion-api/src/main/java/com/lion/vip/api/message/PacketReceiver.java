package com.lion.vip.api.message;

        import com.lion.vip.api.connection.Connection;
        import com.lion.vip.api.protocol.Packet;

/**
 * 数据包接收接口
 */
public interface PacketReceiver {

    void onReceive(Packet packet, Connection connection);
}
