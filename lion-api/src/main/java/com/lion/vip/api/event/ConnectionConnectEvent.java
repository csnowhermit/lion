package com.lion.vip.api.event;

import com.lion.vip.api.connection.Connection;

public class ConnectionConnectEvent implements Event {
    private final Connection connection;

    public ConnectionConnectEvent(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }
}
