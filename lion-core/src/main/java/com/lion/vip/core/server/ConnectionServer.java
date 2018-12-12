package com.lion.vip.core.server;

import com.lion.vip.api.connection.ConnectionManager;
import com.lion.vip.api.protocol.Command;
import com.lion.vip.api.service.Listener;
import com.lion.vip.api.spi.handler.PushHandlerFactory;
import com.lion.vip.common.MessageDispatcher;
import com.lion.vip.core.LionServer;
import com.lion.vip.core.handler.*;
import com.lion.vip.network.netty.server.NettyTCPServer;
import com.lion.vip.tools.config.CC;
import com.lion.vip.tools.thread.NamedPoolThreadFactory;
import com.lion.vip.tools.thread.ThreadNames;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.handler.traffic.GlobalChannelTrafficShapingHandler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.lion.vip.tools.config.CC.lion.net.connect_server_bind_ip;
import static com.lion.vip.tools.config.CC.lion.net.connect_server_port;
import static com.lion.vip.tools.config.CC.lion.net.write_buffer_water_mark.connect_server_high;
import static com.lion.vip.tools.config.CC.lion.net.write_buffer_water_mark.connect_server_low;
import static com.lion.vip.tools.thread.ThreadNames.T_TRAFFIC_SHAPING;

import static com.lion.vip.tools.config.CC.lion.net.traffic_shaping.connect_server.*;

/**
 * 连接服务器
 */
public class ConnectionServer extends NettyTCPServer {

    private LionServer lionServer;    //Lion主服务
    private ConnectionManager connectionManager;    //连接管理器

    private MessageDispatcher messageDispatcher;    //做消息分发

    private ServerChannelHandler channelHandler;    //服务端Channel处理类
    private GlobalChannelTrafficShapingHandler trafficShapingHandler;
    private ScheduledExecutorService trafficShapingExecutor;


    public ConnectionServer(LionServer lionServer) {
        super(connect_server_bind_ip, connect_server_port);
        this.lionServer = lionServer;
        this.connectionManager = new ServerConnectionManager(true);
        this.messageDispatcher = new MessageDispatcher();
        this.channelHandler = new ServerChannelHandler(true, connectionManager, messageDispatcher);
    }


    @Override
    public void init() {
        super.init();
        connectionManager.init();

        //messageDispatcher：做消息分发，这里定义了多种消息类型
        messageDispatcher.register(Command.HEARTBEAT, HeartBeatHandler::new);                     //心跳
        messageDispatcher.register(Command.HANDSHAKE, () -> new HandshakeHandler(lionServer));    //握手
        messageDispatcher.register(Command.BIND, () -> new BindUserHandler(lionServer));          //绑定
        messageDispatcher.register(Command.UNBIND, () -> new BindUserHandler(lionServer));        //解绑
        messageDispatcher.register(Command.FAST_CONNECT, () -> new FastConnectHandler(lionServer));  //快速重连
        messageDispatcher.register(Command.PUSH, PushHandlerFactory::create);                     //消息推送
        messageDispatcher.register(Command.ACK, () -> new AckHandler(lionServer));                //回应
        messageDispatcher.register(Command.HTTP_PROXY, () -> new HttpProxyHandler(lionServer), CC.lion.http.proxy_enabled);  //http代理

        //启用流量整形，限流
        if (CC.lion.net.traffic_shaping.connect_server.enabled) {
            trafficShapingExecutor = Executors.newSingleThreadScheduledExecutor(new NamedPoolThreadFactory(T_TRAFFIC_SHAPING));
            trafficShapingHandler = new GlobalChannelTrafficShapingHandler(
                    trafficShapingExecutor,
                    write_global_limit, read_global_limit,
                    write_channel_limit, read_channel_limit,
                    check_interval);
        }
    }

    @Override
    public void stop(Listener listener) {
        super.stop(listener);

        if (trafficShapingHandler != null) {
            trafficShapingHandler.release();
            trafficShapingExecutor.shutdown();
        }
        connectionManager.destroy();
    }

    @Override
    public void start(Listener listener) {
        super.start(listener);

        //增加线程池监控
        if (this.getWorkerGroup() != null) {
            lionServer.getMonitor().monitor("conn-worker", this.getWorkerGroup());
        }
    }

    @Override
    protected int getWorkThreadNum() {
        return CC.lion.thread.pool.conn_work;
    }

    @Override
    protected String getBossThreadName() {
        return ThreadNames.T_CONN_BOSS;
    }

    @Override
    protected String getWorkThreadName() {
        return ThreadNames.T_CONN_WORKER;
    }

    @Override
    protected void initPipeline(ChannelPipeline pipeline) {
        super.initPipeline(pipeline);
        if (trafficShapingHandler != null) {
            pipeline.addFirst(trafficShapingHandler);
        }
    }

    @Override
    protected void initOptions(ServerBootstrap bootstrap) {
        super.initOptions(bootstrap);

        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);

        /**
         * TCP层面的接收和发送缓冲区大小设置，
         * 在Netty中分别对应ChannelOption的SO_SNDBUF和SO_RCVBUF，
         * 需要根据推送消息的大小，合理设置，对于海量长连接，通常32K是个不错的选择。
         */
        if (CC.lion.net.snd_buf.connect_server > 0)
            bootstrap.childOption(ChannelOption.SO_SNDBUF, CC.lion.net.snd_buf.connect_server);
        if (CC.lion.net.rcv_buf.connect_server > 0)
            bootstrap.childOption(ChannelOption.SO_RCVBUF, CC.lion.net.rcv_buf.connect_server);

        /**
         * 这个坑其实也不算坑，只是因为懒，该做的事情没做。一般来讲我们的业务如果比较小的时候我们用同步处理，等业务到一定规模的时候，一个优化手段就是异步化。
         * 异步化是提高吞吐量的一个很好的手段。但是，与异步相比，同步有天然的负反馈机制，也就是如果后端慢了，前面也会跟着慢起来，可以自动的调节。
         * 但是异步就不同了，异步就像决堤的大坝一样，洪水是畅通无阻。如果这个时候没有进行有效的限流措施就很容易把后端冲垮。
         * 如果一下子把后端冲垮倒也不是最坏的情况，就怕把后端冲的要死不活。
         * 这个时候，后端就会变得特别缓慢，如果这个时候前面的应用使用了一些无界的资源等，就有可能把自己弄死。
         * 那么现在要介绍的这个坑就是关于Netty里的ChannelOutboundBuffer这个东西的。
         * 这个buffer是用在netty向channel write数据的时候，有个buffer缓冲，这样可以提高网络的吞吐量(每个channel有一个这样的buffer)。
         * 初始大小是32(32个元素，不是指字节)，但是如果超过32就会翻倍，一直增长。
         * 大部分时候是没有什么问题的，但是在碰到对端非常慢(对端慢指的是对端处理TCP包的速度变慢，比如对端负载特别高的时候就有可能是这个情况)的时候就有问题了，
         * 这个时候如果还是不断地写数据，这个buffer就会不断地增长，最后就有可能出问题了(我们的情况是开始吃swap，最后进程被linux killer干掉了)。
         * 为什么说这个地方是坑呢，因为大部分时候我们往一个channel写数据会判断channel是否active，但是往往忽略了这种慢的情况。
         *
         * 那这个问题怎么解决呢？其实ChannelOutboundBuffer虽然无界，但是可以给它配置一个高水位线和低水位线，
         * 当buffer的大小超过高水位线的时候对应channel的isWritable就会变成false，
         * 当buffer的大小低于低水位线的时候，isWritable就会变成true。所以应用应该判断isWritable，如果是false就不要再写数据了。
         * 高水位线和低水位线是字节数，默认高水位是64K，低水位是32K，我们可以根据我们的应用需要支持多少连接数和系统资源进行合理规划。
         */
        bootstrap.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(
                connect_server_low, connect_server_high
        ));
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return channelHandler;
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public MessageDispatcher getMessageDispatcher() {
        return messageDispatcher;
    }


}
