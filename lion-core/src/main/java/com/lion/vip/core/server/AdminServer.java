package com.lion.vip.core.server;

import com.lion.vip.core.LionServer;
import com.lion.vip.core.handler.AdminHandler;
import com.lion.vip.network.netty.server.NettyTCPServer;
import com.lion.vip.tools.thread.ThreadNames;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import static com.lion.vip.tools.config.CC.lion.net.connect_server_port;

/**
 * 后台管理员服务器
 */
public class AdminServer extends NettyTCPServer {
    private AdminHandler adminHandler;
    private LionServer lionServer;

    public AdminServer(LionServer lionServer) {
        super(connect_server_port);
        this.lionServer = lionServer;
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return adminHandler;
    }

    @Override
    public void init() {
        super.init();
        this.adminHandler = new AdminHandler(lionServer);
    }

    @Override
    protected void initPipeline(ChannelPipeline pipeline) {
        pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
        super.initPipeline(pipeline);
    }

    @Override
    protected ChannelHandler getDecoder() {
        return new StringDecoder();
    }

    @Override
    protected ChannelHandler getEncoder() {
        return new StringEncoder();
    }

    @Override
    protected int getWorkThreadNum() {
        return 1;
    }

    @Override
    protected String getBossThreadName() {
        return ThreadNames.T_ADMIN_BOSS;
    }

    @Override
    protected String getWorkThreadName() {
        return ThreadNames.T_ADMIN_WORKER;
    }
}
