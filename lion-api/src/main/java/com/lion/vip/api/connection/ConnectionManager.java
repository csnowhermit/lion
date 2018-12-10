package com.lion.vip.api.connection;

import io.netty.channel.Channel;

/**
 * 建立连接
 */
public interface ConnectionManager {

    /**
     * 从通道中获取连接
     *
     * @param channel
     * @return
     */
    Connection get(Channel channel);

    /**
     * 从通道中移除并关闭连接
     *
     * @param channel
     * @return
     */
    Connection removeAndClose(Channel channel);

    /**
     * 添加连接
     *
     * @param connection
     */
    void add(Connection connection);

    /**
     * 获取连接个数
     *
     * @return
     */
    int getConnNum();

    /**
     * 连接管理器初始化
     */
    void init();

    /**
     * 连接管理器销毁
     */
    void destroy();
}
