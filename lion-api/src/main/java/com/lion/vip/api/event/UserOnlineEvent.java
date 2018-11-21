/**
 * FileName: UserOnlineEvent
 * Author:   Ren Xiaotian
 * Date:     2018/11/21 14:02
 */

package com.lion.vip.api.event;

import com.lion.vip.api.connection.Connection;

/**
 * 用户上线事件
 */
public class UserOnlineEvent implements Event {
    private final Connection connection;    //连接
    private final String userId;            //用户ID

    public UserOnlineEvent(Connection connection, String userId) {
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
