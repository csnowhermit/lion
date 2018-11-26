/**
 * FileName: Connection
 * Author:   Ren Xiaotian
 * Date:     2018/11/21 11:25
 */

package com.lion.vip.api.connection;

import com.lion.vip.api.protocol.Packet;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;


/**
 * 连接 接口
 */
public interface Connection {

    byte STATUS_NEW = 0;    //新建连接
    byte STATUS_CONNECTED = 1;    //已连接
    byte STATUS_DISCONNECTED = 2;   //断开连接

    /**
     * 初始化
     *
     * @param channel
     * @param security
     */
    void init(Channel channel, boolean security);

    /**
     * 获取session上下文
     *
     * @return
     */
    SessionContext getSessionContext();

    /**
     * 设置Session上下文
     *
     * @param sessionContext
     */
    void setSessionContext(SessionContext sessionContext);

    /**
     * 发送数据包
     *
     * @param packet
     * @return
     */
    ChannelFuture send(Packet packet);

    /**
     * 发送数据包
     *
     * @param packet
     * @param channelFutureListener
     * @return
     */
    ChannelFuture send(Packet packet, ChannelFutureListener channelFutureListener);

    /**
     * 获取Connection的ID
     *
     * @return
     */
    String getId();

    /**
     * 关闭连接
     *
     * @return
     */
    ChannelFuture close();

    /**
     * 是否已连接
     *
     * @return
     */
    boolean isConnected();

    /**
     * 是否读超时
     *
     * @return
     */
    boolean isReadTimeout();

    /**
     * 是否写超时
     *
     * @return
     */
    boolean isWriteTimeout();

    /**
     * 更新最后一次读时间
     */
    void updateLastReadTime();

    /**
     * 更新最后一次写时间
     */
    void updateLastWriteTime();

    /**
     * 获取通道
     *
     * @return
     */
    Channel getChannel();

}
