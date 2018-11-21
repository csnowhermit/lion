/**
 * FileName: UserOfflineEvent
 * Author:   Ren Xiaotian
 * Date:     2018/11/21 14:00
 */

package com.lion.vip.api.event;

import com.lion.vip.api.connection.Connection;

/**
 * 用户下线事件
 */
public final class UserOfflineEvent implements Event {
    private final Connection connection;    //用户连接
    private final String userId;            //用户ID

    public UserOfflineEvent(Connection connection, String userId) {
        this.connection = connection;
        this.userId = userId;
    }

    public Connection getConnection() {
        return connection;
    }

    public String getUserId() {
        return userId;
    }
}
