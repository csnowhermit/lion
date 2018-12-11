package com.lion.vip.core.server;

import com.lion.vip.api.connection.Connection;
import com.lion.vip.api.connection.ConnectionManager;
import com.lion.vip.common.MessageDispatcher;
import com.lion.vip.core.LionServer;
import com.lion.vip.network.netty.server.NettyTCPServer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.handler.traffic.GlobalChannelTrafficShapingHandler;

import java.util.concurrent.ScheduledExecutorService;

import static com.lion.vip.tools.config.CC.lion.net.gateway_server_bind_ip;
import static com.lion.vip.tools.config.CC.lion.net.gateway_server_port;

/**
 * 网关服务器
 */
public class GatewayServer extends NettyTCPServer {

    private LionServer lionServer;    //lion主服务
    private ConnectionManager connectionManager;    //连接管理器
    private MessageDispatcher messageDispatcher;    //做消息分发

    private ServerChannelHandler channelHandler;
    private GlobalChannelTrafficShapingHandler trafficShapingHandler;
    private ScheduledExecutorService trafficShapingExecutor;

    public GatewayServer(LionServer lionServer) {
        super(gateway_server_bind_ip, gateway_server_port);
        this.lionServer = lionServer;
        this.messageDispatcher = new MessageDispatcher();
        this.connectionManager = new ServerConnectionManager(false);
        this.channelHandler = new ServerChannelHandler(true, connectionManager, messageDispatcher);

    }

    @Override
    public ChannelHandler getChannelHandler() {
        return channelHandler;
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public MessageDispatcher getMessageDispatcher() {
        return messageDispatcher;
    }

    public void setMessageDispatcher(MessageDispatcher messageDispatcher) {
        this.messageDispatcher = messageDispatcher;
    }
}
