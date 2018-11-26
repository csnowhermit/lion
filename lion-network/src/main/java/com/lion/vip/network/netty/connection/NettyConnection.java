/**
 * FileName: NettyConnection
 * Author:   Ren Xiaotian
 * Date:     2018/11/23 15:13
 */

package com.lion.vip.network.netty.connection;

import com.lion.vip.api.connection.Connection;
import com.lion.vip.api.connection.SessionContext;
import com.lion.vip.api.protocol.Packet;
import com.lion.vip.api.spi.core.RSACipherFactory;
import com.lion.vip.tools.log.Logs;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty连接
 */
public class NettyConnection implements Connection, ChannelFutureListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyConnection.class);

    private SessionContext sessionContext;    //session上下文
    private Channel channel;    //通道
    private volatile byte status = STATUS_NEW;    //当前连接的状态，默认：已创建
    private long lastReadTime;      //最后一次读的时间
    private long lastWriteTime;     //最后一次写的时间

    @Override
    public void init(Channel channel, boolean security) {
        this.channel = channel;
        this.sessionContext = new SessionContext();
        this.lastReadTime = System.currentTimeMillis();
        this.status = STATUS_CONNECTED;
        if (security) {
            this.sessionContext.changeCipher(RSACipherFactory.create());
        }
    }

    @Override
    public SessionContext getSessionContext() {
        return this.sessionContext;
    }

    @Override
    public void setSessionContext(SessionContext sessionContext) {
        this.sessionContext = sessionContext;
    }

    @Override
    public ChannelFuture send(Packet packet) {
        return send(packet, null);
    }

    @Override
    public ChannelFuture send(Packet packet, ChannelFutureListener channelFutureListener) {
        if (channel.isActive()) {    //首先判断通道是否可用
            ChannelFuture channelFuture = channel.writeAndFlush(packet.toFrame(channel)).addListener(this);

            if (channelFutureListener != null) {
                channelFuture.addListener(channelFutureListener);
            }
            if (channel.isWritable()) {
                return channelFuture;
            }

            // 如果通道不可写的话，是 抛异常 还是 调用线程阻塞？
            // 抛异常
            return channel.newPromise().setFailure(new RuntimeException("send data too busy"));
//
//            //调用线程阻塞
//            if (channelFuture.channel().eventLoop().inEventLoop()) {
//                channelFuture.awaitUninterruptibly(100);
//            }
//            return channelFuture;
        } else {    //如果通道不可用，则抛异常
            if (channelFutureListener != null) {
                channel.newPromise()
                        .addListener(channelFutureListener)
                        .setFailure(new RuntimeException("Connection is disconnected"));
            }
            return this.close();
        }
    }

    @Override
    public String getId() {
        return this.channel.id().asShortText();
    }

    @Override
    public ChannelFuture close() {
        if (status == STATUS_DISCONNECTED) {    //如果已经关闭，则返回null
            return null;
        }
        //否则，将状态置为已关闭，再关闭channel
        this.status = STATUS_DISCONNECTED;
        return this.channel.close();
    }

    @Override
    public boolean isConnected() {
        return status == STATUS_CONNECTED;
    }

    @Override
    public boolean isReadTimeout() {
        return System.currentTimeMillis() - lastReadTime > sessionContext.getHeartbeat() + 1000;
    }

    @Override
    public boolean isWriteTimeout() {
        return System.currentTimeMillis() - lastWriteTime > sessionContext.getHeartbeat() - 1000;
    }

    @Override
    public void updateLastReadTime() {
        this.lastReadTime = System.currentTimeMillis();
    }

    @Override
    public void updateLastWriteTime() {
        this.lastWriteTime = System.currentTimeMillis();
    }

    @Override
    public Channel getChannel() {
        return this.channel;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (future.isSuccess()) {
            lastWriteTime = System.currentTimeMillis();
        } else {
            LOGGER.error("Connection send message error: {}", future.cause());
            Logs.CONN.error("connection send msg error={}, conn={}", future.cause().getMessage(), this);
        }
    }

    @Override
    public String toString() {
        return "NettyConnection{" +
                "sessionContext=" + sessionContext +
                ", channel=" + channel +
                ", status=" + status +
                ", lastReadTime=" + lastReadTime +
                ", lastWriteTime=" + lastWriteTime +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        NettyConnection that = (NettyConnection) obj;

        return channel.id().equals(that.channel.id());
    }

    @Override
    public int hashCode() {
        return this.channel.id().hashCode();
    }
}
