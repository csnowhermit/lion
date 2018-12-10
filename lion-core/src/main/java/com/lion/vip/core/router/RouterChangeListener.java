package com.lion.vip.core.router;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.lion.vip.api.connection.Connection;
import com.lion.vip.api.connection.SessionContext;
import com.lion.vip.api.event.RouterChangeEvent;
import com.lion.vip.api.router.ClientLocation;
import com.lion.vip.api.router.Router;
import com.lion.vip.api.spi.common.MQClient;
import com.lion.vip.api.spi.common.MQClientFactory;
import com.lion.vip.api.spi.common.MQMessageReceiver;
import com.lion.vip.common.message.KickUserMessage;
import com.lion.vip.common.router.RemoteRouter;
import com.lion.vip.core.LionServer;
import com.lion.vip.tools.config.CC;
import com.lion.vip.tools.event.EventConsumer;
import com.lion.vip.tools.log.Logs;

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
     * @param userId
     * @param remoteRouter
     */
    private void sendKickUserMessage2MQ(String userId, RemoteRouter remoteRouter) {
        ClientLocation location = remoteRouter.getRouteValue();

        //1.如果机器是当前机器，则不用广播，直接忽略
        if (lionServer.isTargetmachine(location.getHost(), location.getPort())){
            Logs.CONN.debug("kick remote router in local server, ignore remotw broadcast, userId={}", userId);
            return;
        }

        if (udpGateway){

        }

//        if (udpGateway) {
//            Connection connection = lionServer.getUdpGatewayServer().getConnection();
//            GatewayKickUserMessage.build(connection)
//                    .setUserId(userId)
//                    .setClientType(location.getClientType())
//                    .setConnId(location.getConnId())
//                    .setDeviceId(location.getDeviceId())
//                    .setTargetServer(location.getHost())
//                    .setTargetPort(location.getPort())
//                    .setRecipient(new InetSocketAddress(location.getHost(), location.getPort()))
//                    .sendRaw();
//        } else {
//            //2.发送广播
//            //TODO 远程机器可能不存在，需要确认下redis 那个通道如果机器不存在的话，是否会存在消息积压的问题。
//            MQKickRemoteMsg message = new MQKickRemoteMsg()
//                    .setUserId(userId)
//                    .setClientType(location.getClientType())
//                    .setConnId(location.getConnId())
//                    .setDeviceId(location.getDeviceId())
//                    .setTargetServer(location.getHost())
//                    .setTargetPort(location.getPort());
//            mqClient.publish(Constants.getKickChannel(location.getHostAndPort()), message);
//        }

    }

    @Override
    public void receive(String topic, Object message) {

    }
}
