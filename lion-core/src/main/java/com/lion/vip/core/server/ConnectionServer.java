/**
 * FileName: LionServer
 * Author:   Ren Xiaotian
 * Date:     2018/11/19 20:29
 */

package com.lion.vip.core.server;

import com.lion.vip.api.connection.ConnectionManager;
import com.lion.vip.core.LionServer;

/**
 * 连接服务器
 */
public class ConnectionServer {

    private LionServer lionServer;    //Lion主服务
    private ConnectionManager connectionManager;    //连接管理器

    public ConnectionServer(LionServer lionServer) {
        this.lionServer = lionServer;

    }
}
