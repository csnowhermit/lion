package com.lion.vip.core.router;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.lion.vip.api.Constants;
import com.lion.vip.api.connection.Connection;
import com.lion.vip.api.connection.SessionContext;
import com.lion.vip.api.event.RouterChangeEvent;
import com.lion.vip.api.router.ClientLocation;
import com.lion.vip.api.router.Router;
import com.lion.vip.api.spi.common.MQClient;
import com.lion.vip.api.spi.common.MQClientFactory;
import com.lion.vip.api.spi.common.MQMessageReceiver;
import com.lion.vip.common.message.KickUserMessage;
import com.lion.vip.common.message.gateway.GatewayKickUserMessage;
import com.lion.vip.common.router.KickRemoteMsg;
import com.lion.vip.common.router.MQKickRemoteMsg;
import com.lion.vip.common.router.RemoteRouter;
import com.lion.vip.core.LionServer;
import com.lion.vip.tools.Jsons;
import com.lion.vip.tools.config.CC;
import com.lion.vip.tools.config.ConfigTools;
import com.lion.vip.tools.event.EventConsumer;
import com.lion.vip.tools.log.Logs;

import java.net.InetSocketAddress;

import static com.lion.vip.api.Constants.KICK_CHANNEL_PREFIX;

/**
 * 路由变更监听器
 */
public final class RouterChangeListener extends EventConsumer implements MQMessageReceiver {

    private final boolean udpGateway = CC.lion.net.udpGateway();
    private String kick_channel;
    private MQClient mqClient;
    private LionServer lionServer;

    public RouterChangeListener(LionServer lionServer) {
        this.lionServer = lionServer;
        this.kick_channel = KICK_CHANNEL_PREFIX + lionServer.getGatewayServerNode().hostAndPort();
        if (!udpGateway) {
            mqClient = MQClientFactory.create();
            mqClient.init(lionServer);
            mqClient.subscribe(getKickChannel(), this);
        }
    }

    public String getKickChannel() {
        return kick_channel;
    }

    /**
     * 路由变更事件的处理
     *
     * @param event
     */
    @Subscribe
    @AllowConcurrentEvents
    void on(RouterChangeEvent event) {
        String userId = event.userId;
        Router<?> router = event.router;

        if (router.getRouterType().equals(Router.RouterType.LOCAL)) {
            sendKickUserMessage2Client(userId, (LocalRouter) router);
        } else {
            sendKickUserMessage2MQ(userId, (RemoteRouter) router);
        }
    }

    /**
     * 发送踢人消息到客户端
     *
     * @param userId
     * @param router
     */
    private void sendKickUserMessage2Client(String userId, final LocalRouter router) {
        Connection connection = router.getRouteValue();
        SessionContext context = connection.getSessionContext();
        KickUserMessage message = KickUserMessage.build(connection);
        message.deviceId = context.deviceId;
        message.userId = userId;
        message.send(future -> {
            if (future.isSuccess()) {
                Logs.CONN.info("kick local connection success, userId={}, router={}, conn={}", userId, router, connection);
            } else {
                Logs.CONN.warn("kick local connection failure, userId={}, router={}, conn={}", userId, router, connection);
            }
        });
    }

    /**
     * 发送踢人消息到MQ
     *
     * @param userId
     * @param remoteRouter
     */
    private void sendKickUserMessage2MQ(String userId, RemoteRouter remoteRouter) {
        ClientLocation location = remoteRouter.getRouteValue();

        //1.如果机器是当前机器，则不用广播，直接忽略
        if (lionServer.isTargetMachine(location.getHost(), location.getPort())) {
            Logs.CONN.debug("kick remote router in local server, ignore remotw broadcast, userId={}", userId);
            return;
        }

        if (udpGateway) {
            Connection connection = lionServer.getUdpGatewayServer().getConnection();
            GatewayKickUserMessage.build(connection)
                    .setUserId(userId)
                    .setClientType(location.getClientType())
                    .setConnId(location.getConnId())
                    .setDeviceId(location.getDeviceId())
                    .setTargetServer(location.getHost())
                    .setTargetPort(location.getPort())
                    .setRecipient(new InetSocketAddress(location.getHost(), location.getPort()))
                    .sendRaw();
        } else {
            //2.发送广播
            //todo 远程机器可能不存在，需要确认下redis那个通道，如果机器不存在的话，是否会存在消息积压的问题
            MQKickRemoteMsg mqKickRemoteMsg = new MQKickRemoteMsg()
                    .setUserId(userId)
                    .setClientType(location.getClientType())
                    .setConnId(location.getConnId())
                    .setDeviceId(location.getDeviceId())
                    .setTargetServer(location.getHost())
                    .setTargetPort(location.getPort());
            mqClient.publish(Constants.getKickChannel(location.getHostAndPort()), mqKickRemoteMsg);

        }
    }

    @Override
    public void receive(String topic, Object message) {
        if (getKickChannel().equals(topic)) {
            KickRemoteMsg msg = Jsons.fromJson(message.toString(), MQKickRemoteMsg.class);
            if (msg != null) {
                onReceiveKickRemoteMsg(msg);
            } else {
                Logs.CONN.warn("receive an error kick message={}", message);
            }
        } else {
            Logs.CONN.warn("receive an error redis channel={}", topic);
        }
    }

    /**
     * 处理远程机器的踢人广播
     *
     * @param msg
     */
    public void onReceiveKickRemoteMsg(KickRemoteMsg msg) {
        //1如果当前机器不是目标机器，直接忽略
        if (!lionServer.isTargetMachine(msg.getTargetServer(), msg.getTargetPort())) {
            Logs.CONN.error("receive kick remote msg, target server error, localIP={}, msg={}", ConfigTools.getLocalIp(), msg);
            return;
        }

        //2.查询本地路由，找到要被踢下线的连接，并删除该本地路由
        String userId = msg.getUserId();
        int clientType = msg.getClientType();
        LocalRouterManager localRouterManager = lionServer.getRouterCenter().getLocalRouterManager();
        LocalRouter localRouter = localRouterManager.lookup(userId, clientType);

        if (localRouter != null) {    //如果存在本地路由，则进行踢人
            Logs.CONN.info("receive kick remote msg, msg={}", msg);
            if (localRouter.getRouteValue().getId().equals(msg.getConnId())) {    //二次校验，防止误杀
                //fix 0.8.1 踢人的时候不再主动删除路由信息，只发踢人消息到客户端，路由信息有客户端主动解绑的时候再处理。
                //2.1删除本地路由信息
                localRouterManager.unregister(userId, clientType);
                //2.2发送踢人消息到客户端
                sendKickUserMessage2Client(userId, localRouter);
            } else {    //二次校验未通过
                Logs.CONN.warn("kick router failure target connId not match, localRouter={}, msg={}", localRouter, msg);
            }
        } else {
            Logs.CONN.warn("kick router failure can not find local router, msg={}", msg);
        }

    }
}
