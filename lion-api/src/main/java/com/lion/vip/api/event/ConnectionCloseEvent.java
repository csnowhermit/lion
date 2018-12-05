/**
 * FileName: ConnectionCloseEvent
 * Author:   Ren Xiaotian
 * Date:     2018/11/21 14:04
 */

package com.lion.vip.api.event;

import com.lion.vip.api.connection.Connection;

/**
 * 连接关闭事件
 */
public class ConnectionCloseEvent implements Event {
    public final Connection connection;

    public ConnectionCloseEvent(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }
}
