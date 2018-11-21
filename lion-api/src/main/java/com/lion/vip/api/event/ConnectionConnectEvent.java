/**
 * FileName: ConnectionConnectEvent
 * Author:   Ren Xiaotian
 * Date:     2018/11/21 14:05
 */

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
