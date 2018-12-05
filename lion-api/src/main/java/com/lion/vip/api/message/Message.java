package com.lion.vip.api.message;

import com.lion.vip.api.connection.Connection;
import com.lion.vip.api.protocol.Packet;
import io.netty.channel.ChannelFutureListener;

public interface Message {

    /**
     * 获得连接
     *
     * @return
     */
    Connection getConnection();

    /**
     * 解码body
     */
    void decodeBody();

    /**
     * 编码body
     */
    void encodeBody();

    /**
     * 发送当前message，并根据情况对body进行数据压缩，加密
     *
     * @param listener 发送成功后的回调
     */
    void send(ChannelFutureListener listener);

    /**
     * 发送当前message，不会对body进行数据压缩、加密，原样发送
     *
     * @param listener 发送成功后的回调
     */
    void sendRaw(ChannelFutureListener listener);

    /**
     * 获取数据包
     *
     * @return
     */
    Packet getPacket();
}
