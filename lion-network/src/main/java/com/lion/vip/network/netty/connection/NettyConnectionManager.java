package com.lion.vip.network.netty.connection;

import com.lion.vip.api.connection.Connection;
import com.lion.vip.api.connection.ConnectionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelId;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Netty 连接管理器
 */
public class NettyConnectionManager implements ConnectionManager {

    private final ConcurrentHashMap<ChannelId, Connection> connectionConcurrentHashMap = new ConcurrentHashMap<>();

    @Override
    public Connection get(Channel channel) {
        return connectionConcurrentHashMap.get(channel.id());
    }

    @Override
    public Connection removeAndClose(Channel channel) {
        return connectionConcurrentHashMap.remove(channel.id());
    }

    @Override
    public void add(Connection connection) {
        connectionConcurrentHashMap.putIfAbsent(connection.getChannel().id(), connection);
    }

    @Override
    public int getConnNum() {
        return connectionConcurrentHashMap.size();
    }

    @Override
    public void init() {

    }

    /**
     * 销毁连接管理器：先清空容器，关闭所有连接；再销毁
     */
    @Override
    public void destroy() {
        connectionConcurrentHashMap.values().forEach(Connection::close);
        connectionConcurrentHashMap.clear();
    }
}
