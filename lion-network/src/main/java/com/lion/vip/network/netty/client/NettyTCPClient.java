package com.lion.vip.network.netty.client;

import com.lion.vip.api.service.BaseService;
import com.lion.vip.api.service.Client;
import com.lion.vip.api.service.Listener;
import com.lion.vip.network.netty.codec.PacketDecoder;
import com.lion.vip.network.netty.codec.PacketEncoder;
import com.lion.vip.tools.thread.ThreadNames;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.channels.spi.SelectorProvider;

import static com.lion.vip.tools.Utils.useNettyEpoll;

/**
 * Netty TCP客户端
 */
public abstract class NettyTCPClient extends BaseService implements Client {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyTCPClient.class);

    private EventLoopGroup eventLoopGroup;
    private Bootstrap bootstrap;

    private void createClient(Listener listener, EventLoopGroup eventExecutors, ChannelFactory<? extends Channel> channelFactory) {
        this.eventLoopGroup = eventExecutors;
        this.bootstrap = bootstrap;

        this.bootstrap.group(eventExecutors)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .channelFactory(channelFactory)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        initPipeLine(ch);
                    }
                });

        initOptions(bootstrap);
        listener.onSuccess();
    }

    private void initOptions(Bootstrap bootstrap) {
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 4000);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
    }

    private void initPipeLine(SocketChannel ch) {
        ChannelPipeline channelPipeline = ch.pipeline();
        channelPipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));    //避免粘包问题
        channelPipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
        channelPipeline.addLast("dncoder", new StringDecoder(CharsetUtil.UTF_8));
        channelPipeline.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));


    }

    protected void initPipeline(ChannelPipeline pipeline) {
        pipeline.addLast("decoder", getDecoder());
        pipeline.addLast("encoder", getEncoder());
        pipeline.addLast("handler", getChannelHandler());
    }

    private ChannelHandler getDecoder() {
        return new PacketDecoder();
    }

    private ChannelHandler getEncoder() {
        return PacketEncoder.INSTANCE;
    }

    public abstract ChannelHandler getChannelHandler();

    public ChannelFuture connect(String host, int port) {
        return bootstrap.connect(new InetSocketAddress(host, port));
    }

    public ChannelFuture connect(String host, int port, Listener listener) {
        return bootstrap.connect(new InetSocketAddress(host, port)).addListener(f -> {
            if (f.isSuccess()) {
                if (listener != null) {
                    listener.onSuccess(port);
                    LOGGER.info("start netty client success, host={}, port={}", host, port);
                }
            } else {
                if (listener != null) {
                    listener.onFailure(f.cause());
                    LOGGER.error("start netty client failure, host={}, port={}", host, port, f.cause());
                }
            }
        });
    }

    private void createNioClient(Listener listener) {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(getWorkerThreadNum(),
                new DefaultThreadFactory(ThreadNames.T_TCP_CLIENT), getSelectorProvider());
        workerGroup.setIoRatio(getIORate());
        createClient(listener, workerGroup, getChannelFactory());
    }

    private ChannelFactory<? extends Channel> getChannelFactory() {
        return NioSocketChannel::new;
    }

    private SelectorProvider getSelectorProvider() {
        return SelectorProvider.provider();
    }

    private void createEpollClient(Listener listener) {
        EpollEventLoopGroup workerGroup = new EpollEventLoopGroup(getWorkerThreadNum(),
                new DefaultThreadFactory(ThreadNames.T_TCP_CLIENT));

        workerGroup.setIoRatio(getIORate());
        createClient(listener, workerGroup, EpollDatagramChannel::new);
    }

    private int getIORate() {
        return 50;
    }

    private int getWorkerThreadNum() {
        return 1;
    }

    @Override
    protected void doStart(Listener listener) throws Throwable {
        if (useNettyEpoll()) {
            createEpollClient(listener);
        } else {
            createNioClient(listener);
        }
    }

    @Override
    protected void doStop(Listener listener) throws Throwable {
        if (eventLoopGroup != null) {
            eventLoopGroup.shutdownGracefully();
        }
        LOGGER.error("netty client [{}] stopped.", this.getClass().getSimpleName());
        listener.onSuccess();
    }

    @Override
    public String toString() {
        return "NettyTCPClient{" +
                this.getClass().getSimpleName();
    }
}
