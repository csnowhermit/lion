package com.lion.vip.client.gateway;

import com.lion.vip.api.connection.Connection;
import com.lion.vip.api.protocol.Command;
import com.lion.vip.api.service.Listener;
import com.lion.vip.client.LionClient;
import com.lion.vip.client.gateway.handler.GatewayErrorHandler;
import com.lion.vip.client.gateway.handler.GatewayOKHandler;
import com.lion.vip.common.MessageDispatcher;
import com.lion.vip.network.netty.udp.NettyUDPConnector;
import com.lion.vip.network.netty.udp.UDPChannelHandler;
import com.lion.vip.tools.Utils;
import com.lion.vip.tools.config.CC;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;

import static com.lion.vip.common.MessageDispatcher.POLICY_LOG;

public final class GatewayUDPConnector extends NettyUDPConnector {

    private UDPChannelHandler channelHandler;
    private MessageDispatcher messageDispatcher;
    private LionClient lionClient;

    public GatewayUDPConnector(LionClient lionClient) {
        super(CC.lion.net.gateway_client_port);
        this.lionClient = lionClient;
        this.messageDispatcher = new MessageDispatcher(POLICY_LOG);
    }

    @Override
    public void init() {
        super.init();
        messageDispatcher.register(Command.OK, () -> new GatewayOKHandler(lionClient));
        messageDispatcher.register(Command.ERROR, () -> new GatewayErrorHandler(lionClient));
        channelHandler = new UDPChannelHandler(messageDispatcher);
        channelHandler.setMulticastAddress(Utils.getInetAddress(CC.lion.net.gateway_client_multicast));
        channelHandler.setNetworkInterface(Utils.getLocalNetworkInterface());
    }


    @Override
    public void stop(Listener listener) {
        super.stop(listener);
    }


    @Override
    protected void initOptions(Bootstrap b) {
        super.initOptions(b);
        b.option(ChannelOption.IP_MULTICAST_LOOP_DISABLED, true);
        b.option(ChannelOption.IP_MULTICAST_TTL, 255);
        if (CC.lion.net.snd_buf.gateway_client > 0) {
            b.option(ChannelOption.SO_SNDBUF, CC.lion.net.snd_buf.gateway_client);
        }
        if (CC.lion.net.rcv_buf.gateway_client > 0) {
            b.option(ChannelOption.SO_RCVBUF, CC.lion.net.rcv_buf.gateway_client);
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
