
package com.lion.vip.client.connect;

import com.google.common.eventbus.Subscribe;
import com.lion.vip.api.event.ConnectionCloseEvent;
import com.lion.vip.network.netty.client.NettyTCPClient;
import com.lion.vip.tools.event.EventBus;
import io.netty.channel.ChannelHandler;

public class ConnectClient extends NettyTCPClient {
    private final ConnectClientChannelHandler handler;

    public ConnectClient(String host, int port, ClientConfig config) {
        handler = new ConnectClientChannelHandler(config);
        EventBus.register(this);
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return handler;
    }

    @Subscribe
    void on(ConnectionCloseEvent event) {
        this.stop();
    }

    protected int getWorkThreadNum() {
        return 1;
    }
}
