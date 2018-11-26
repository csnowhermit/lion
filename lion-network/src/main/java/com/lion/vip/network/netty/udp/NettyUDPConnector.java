/**
 * FileName: NettyUDPConnector
 * Author:   Ren Xiaotian
 * Date:     2018/11/26 9:14
 */

package com.lion.vip.network.netty.udp;

import com.lion.vip.api.service.BaseService;
import com.lion.vip.api.service.Listener;
import com.lion.vip.api.service.Server;
import com.lion.vip.api.service.ServiceException;
import com.lion.vip.tools.thread.ThreadNames;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * netty 的UDP连接
 */
public abstract class NettyUDPConnector extends BaseService implements Server {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final int port;
    private EventLoopGroup eventLoopGroup;

    public NettyUDPConnector(int port) {
        this.port = port;
    }

    @Override
    protected void doStart(Listener listener) throws Throwable {
        createNioServer(listener);
    }

    private void createNioServer(Listener listener) {
        NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup(
                1, new DefaultThreadFactory(ThreadNames.T_GATEWAY_WORKER)
        );

        nioEventLoopGroup.setIoRatio(100);

        //默认是根据机器情况创建Channel，如果机器支持IPv6，则无法使用IPv4的地址加入组播
        createServer(listener, nioEventLoopGroup, () -> new NioDatagramChannel(InternetProtocolFamily.IPv4));
    }

    private void createServer(Listener listener, EventLoopGroup eventLoopGroup, ChannelFactory<? extends DatagramChannel> channelFactory) {
        this.eventLoopGroup = eventLoopGroup;
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channelFactory(channelFactory)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(getChannelHandler());
            initOptions(bootstrap);

            //直接绑定端口，不指定host，不然收不到组播消息
            bootstrap.bind(port).addListener(future -> {
                if (future.isSuccess()) {
                    LOGGER.info("udp server start success on : {}", port);
                    if (listener != null) {
                        listener.onSuccess(port);
                    }
                } else {
                    LOGGER.info("udp server start failyre on : {}", port);
                    if (listener != null) {
                        listener.onFailure(future.cause());
                    }
                }
            });
        } catch (Exception e) {
            LOGGER.info("udp server start exception: {}", e);
            if (listener != null) {
                listener.onFailure(e);
            }
            throw new ServiceException("udp server start exception, port = " + port, e);
        }
    }

    private void initOptions(Bootstrap bootstrap) {
        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.option(ChannelOption.SO_REUSEADDR, true);
    }

    public abstract ChannelHandler getChannelHandler();


    @Override
    protected void doStop(Listener listener) throws Throwable {
        LOGGER.info("try shutdown {} ...", this.getClass().getSimpleName());
        if (eventLoopGroup != null) {
            eventLoopGroup.shutdownGracefully().syncUninterruptibly();
        }
        LOGGER.info("{} shutdown success.", this.getClass().getSimpleName());
        listener.onSuccess(port);
    }

    @SuppressWarnings("unused")
    private void createEpollServer(Listener listener) {
        EpollEventLoopGroup epollEventLoopGroup = new EpollEventLoopGroup(
                1, new DefaultThreadFactory(ThreadNames.T_GATEWAY_WORKER)
        );
        epollEventLoopGroup.setIoRatio(100);
        createServer(listener, epollEventLoopGroup, EpollDatagramChannel::new);
    }

}
