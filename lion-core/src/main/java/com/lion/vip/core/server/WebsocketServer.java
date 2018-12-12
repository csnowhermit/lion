package com.lion.vip.core.server;

import com.lion.vip.api.connection.ConnectionManager;
import com.lion.vip.api.protocol.Command;
import com.lion.vip.api.service.Listener;
import com.lion.vip.api.spi.handler.PushHandlerFactory;
import com.lion.vip.common.MessageDispatcher;
import com.lion.vip.core.LionServer;
import com.lion.vip.core.handler.AckHandler;
import com.lion.vip.core.handler.BindUserHandler;
import com.lion.vip.core.handler.HandshakeHandler;
import com.lion.vip.network.netty.server.NettyTCPServer;
import com.lion.vip.tools.config.CC;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;

import static com.lion.vip.tools.config.CC.lion.net.connect_server_bind_ip;
import static com.lion.vip.tools.config.CC.lion.net.connect_server_port;

/**
 * websocket服务器
 */
public class WebsocketServer extends NettyTCPServer {
    private final ChannelHandler channelHandler;
    private final MessageDispatcher messageDispatcher;
    private final ConnectionManager connectionManager;
    private final LionServer lionServer;

    public WebsocketServer(LionServer lionServer) {
        super(connect_server_bind_ip, connect_server_port);
        this.lionServer = lionServer;
        this.messageDispatcher = new MessageDispatcher();
        this.connectionManager = new ServerConnectionManager(false);
        this.channelHandler = new WebSocketChannelHandler(connectionManager, messageDispatcher);
    }

    @Override
    public void init() {
        super.init();
        connectionManager.init();
        messageDispatcher.register(Command.HANDSHAKE, () -> new HandshakeHandler(lionServer));
        messageDispatcher.register(Command.BIND, () -> new BindUserHandler(lionServer));
        messageDispatcher.register(Command.UNBIND, () -> new BindUserHandler(lionServer));
        messageDispatcher.register(Command.PUSH, PushHandlerFactory::create);
        messageDispatcher.register(Command.ACK, () -> new AckHandler(lionServer));
    }

    @Override
    public EventLoopGroup getBossGroup() {
        return lionServer.getConnectionServer().getBossGroup();
    }

    @Override
    public EventLoopGroup getWorkerGroup() {
        return lionServer.getConnectionServer().getWorkerGroup();
    }

    @Override
    public void stop(Listener listener) {
        super.stop(listener);
        connectionManager.destroy();
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return channelHandler;
    }

    public MessageDispatcher getMessageDispatcher() {
        return messageDispatcher;
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    @Override
    protected void initPipeline(ChannelPipeline pipeline) {
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new WebSocketServerCompressionHandler());
        pipeline.addLast(new WebSocketServerProtocolHandler(CC.lion.net.ws_path, null, true));
        pipeline.addLast(getChannelHandler());
    }

    @Override
    protected void initOptions(ServerBootstrap serverBootstrap) {
        super.initOptions(serverBootstrap);
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        serverBootstrap.option(ChannelOption.SO_SNDBUF, 32 * 1024);
        serverBootstrap.option(ChannelOption.SO_RCVBUF, 32 * 1024);
    }
}
