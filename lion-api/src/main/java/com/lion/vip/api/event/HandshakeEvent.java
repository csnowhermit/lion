package com.lion.vip.api.event;

import com.lion.vip.api.connection.Connection;

/**
 * 握手事件
 */
public final class HandshakeEvent implements Event {
    private final Connection connection;    //连接
    private final int heartbeat;            //心跳检测周期

    public HandshakeEvent(Connection connection, int heartbeat) {
        this.connection = connection;
        this.heartbeat = heartbeat;
    }
}
