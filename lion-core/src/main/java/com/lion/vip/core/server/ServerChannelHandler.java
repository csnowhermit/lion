package com.lion.vip.core.server;

import com.lion.vip.api.connection.Connection;
import com.lion.vip.api.connection.ConnectionManager;
import com.lion.vip.api.event.ConnectionCloseEvent;
import com.lion.vip.api.message.PacketReceiver;
import com.lion.vip.api.protocol.Command;
import com.lion.vip.api.protocol.Packet;
import com.lion.vip.network.netty.connection.NettyConnection;
import com.lion.vip.tools.common.Profiler;
import com.lion.vip.tools.config.CC;
import com.lion.vip.tools.event.EventBus;
import com.lion.vip.tools.log.Logs;
import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务端Channel处理类
 */
@ChannelHandler.Sharable
public class ServerChannelHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerChannelHandler.class);

    private static final long profile_slowly_limit = CC.lion.monitor.profile_slowly_duration.toMillis();

    private final boolean security;    //是否启用加密
    private final ConnectionManager connectionManager;    //连接管理器
    private final PacketReceiver receiver;    //数据包接收器

    public ServerChannelHandler(boolean security, ConnectionManager conncectionManager, PacketReceiver receiver) {
        this.security = security;
        this.connectionManager = conncectionManager;
        this.receiver = receiver;
    }

    //override ChannelInboundHandlerAdapter

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Logs.CONN.info("client connected conn={}", ctx.channel());

        Connection connection = new NettyConnection();
        connection.init(ctx.channel(), security);
        connectionManager.add(connection);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Connection connection = connectionManager.removeAndClose(ctx.channel());

        EventBus.post(new ConnectionCloseEvent(connection));
        Logs.CONN.info("client disconnected conn={}", connection);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Packet packet = (Packet) msg;
        byte cmd = packet.cmd;   //数据包命令

        try {
            Profiler.start("time cost on [channel read]: ", packet.toString());
            Connection connection = connectionManager.get(ctx.channel());    //从连接管理器中拿到一个连接
            LOGGER.debug("channelRead conn={}, packet={}, ctx.channel={}", ctx.channel(), connection.getSessionContext(), msg);
            connection.updateLastReadTime();    //更新这个连接的最后读时间
            receiver.onReceive(packet, connection);
        } finally {
            Profiler.release();
            if (Profiler.getDuration() > profile_slowly_limit) {
                Logs.PROFILE.info("Read Packet[cmd={}] Slowly: \n{}", Command.toCMD(cmd), Profiler.dump());
            }
            Profiler.reset();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Connection connection = connectionManager.get(ctx.channel());
        Logs.CONN.error("client caught ex, conn={}", connection);
        LOGGER.error("caught an ex, channel={}, conn={}", ctx.channel(), connection, cause);
        ctx.close();
    }
}
