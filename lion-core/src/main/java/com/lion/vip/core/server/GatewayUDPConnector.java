package com.lion.vip.core.server;

import com.lion.vip.api.connection.Connection;
import com.lion.vip.api.protocol.Command;
import com.lion.vip.common.MessageDispatcher;
import com.lion.vip.core.LionServer;
import com.lion.vip.core.handler.GatewayKickUserHandler;
import com.lion.vip.core.handler.GatewayPushHandler;
import com.lion.vip.network.netty.udp.NettyUDPConnector;
import com.lion.vip.network.netty.udp.UDPChannelHandler;
import com.lion.vip.tools.Utils;
import com.lion.vip.tools.config.CC;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;

import static com.lion.vip.tools.config.CC.lion.net.gateway_server_multicast;
import static com.lion.vip.tools.config.CC.lion.net.gateway_server_port;

/**
 * UDP网关连接器
 */
public final class GatewayUDPConnector extends NettyUDPConnector {
    private UDPChannelHandler channelHandler;
    private MessageDispatcher messageDispatcher;    //消息分发
    private LionServer lionServer;

    public GatewayUDPConnector(LionServer lionServer) {
        super(gateway_server_port);
        this.lionServer = lionServer;
        this.messageDispatcher = new MessageDispatcher(MessageDispatcher.POLICY_LOG);
        this.channelHandler = new UDPChannelHandler(messageDispatcher);
    }

    @Override
    public void init() {
        super.init();
        //消息分发，注册不同类型的消息及不同的处理类
        messageDispatcher.register(Command.GATEWAY_PUSH, () -> new GatewayPushHandler(lionServer.getPushCenter()));
        messageDispatcher.register(Command.GATEWAY_KICK, () -> new GatewayKickUserHandler(lionServer.getRouterCenter()));

        channelHandler.setMulticastAddress(Utils.getInetAddress(gateway_server_multicast));
        channelHandler.setNetworkInterface(Utils.getLocalNetworkInterface());
    }


    @Override
    protected void initOptions(Bootstrap bootstrap) {
        super.initOptions(bootstrap);
        bootstrap.option(ChannelOption.IP_MULTICAST_LOOP_DISABLED, true);    //默认情况下，当本机发送组播数据到某个网络接口时，在IP层，数据会回送到本地的回环接口，选项IP_MULTICAST_LOOP用于控制数据是否回送到本地的回环接口
        bootstrap.option(ChannelOption.IP_MULTICAST_TTL, 255);               //选项IP_MULTICAST_TTL允许设置超时TTL，范围为0～255之间的任何值

        //b.option(ChannelOption.IP_MULTICAST_IF, null);//选项IP_MULTICAST_IF用于设置组播的默认网络接口，会从给定的网络接口发送，另一个网络接口会忽略此数据,参数addr是希望多播输出接口的IP地址，使用INADDR_ANY地址回送到默认接口。
        //b.option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(32 * 1024, 1024 * 1024));

        if (CC.lion.net.snd_buf.gateway_server > 0) {
            bootstrap.option(ChannelOption.SO_SNDBUF, CC.lion.net.snd_buf.gateway_server);
        }
        if (CC.lion.net.rcv_buf.gateway_server > 0) {
            bootstrap.option(ChannelOption.SO_RCVBUF, CC.lion.net.rcv_buf.gateway_server);
        }
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return channelHandler;
    }

    public Connection getConnection() {
        return channelHandler.getConnection();
    }

    public MessageDispatcher getMessageDispatcher() {
        return messageDispatcher;
    }
}
