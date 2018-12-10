package com.lion.vip.core.router;

import com.lion.vip.api.connection.Connection;
import com.lion.vip.api.event.RouterChangeEvent;
import com.lion.vip.api.router.ClientLocation;
import com.lion.vip.api.router.Router;
import com.lion.vip.api.service.BaseService;
import com.lion.vip.api.service.Listener;
import com.lion.vip.common.router.RemoteRouter;
import com.lion.vip.common.router.RemoteRouterManager;
import com.lion.vip.core.LionServer;
import com.lion.vip.tools.event.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 路由中心
 */
public class RouterCenter extends BaseService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RouterCenter.class);

    private LocalRouterManager localRouterManager;
    private RemoteRouterManager remoteRouterManager;
    private UserEventConsumer userEventConsumer;
    private RouterChangeListener routerChangeListener;
    private LionServer lionServer;

    public RouterCenter(LionServer lionServer) {
        this.lionServer = lionServer;
    }

    public LocalRouterManager getLocalRouterManager() {
        return localRouterManager;
    }

    public RemoteRouterManager getRemoteRouterManager() {
        return remoteRouterManager;
    }

    public UserEventConsumer getUserEventConsumer() {
        return userEventConsumer;
    }

    public RouterChangeListener getRouterChangeListener() {
        return routerChangeListener;
    }

    @Override
    protected void doStart(Listener listener) throws Throwable {
        this.localRouterManager = new LocalRouterManager();
        this.remoteRouterManager = new RemoteRouterManager();
        this.routerChangeListener = new RouterChangeListener(lionServer);
        this.userEventConsumer = new UserEventConsumer(remoteRouterManager);
        userEventConsumer.getUserManager().clearOnlineUserList();

        super.doStart(listener);
    }

    @Override
    protected void doStop(Listener listener) throws Throwable {
        userEventConsumer.getUserManager().clearOnlineUserList();
        super.doStop(listener);
    }

    /**
     * 用户终端注册
     *
     * @param userId
     * @param connection
     * @return
     */
    public boolean register(String userId, Connection connection) {
        ClientLocation location = ClientLocation
                .from(connection)
                .setHost(lionServer.getGatewayServerNode().getHost())
                .setPort(lionServer.getGatewayServerNode().getPort());

        LocalRouter localRouter = new LocalRouter(connection);    //本地路由
        RemoteRouter remoteRouter = new RemoteRouter(location);  //远端路由

        LocalRouter oldLocalRouter = null;
        RemoteRouter oldRemoteRouter = null;

        try {
            oldLocalRouter = localRouterManager.register(userId, localRouter);
            oldRemoteRouter = remoteRouterManager.register(userId, remoteRouter);
        } catch (Exception e) {
            LOGGER.error("register router ex, userId={}, connection={}", userId, connection, e);
        }

        if (oldLocalRouter != null) {
            EventBus.post(new RouterChangeEvent(userId, oldLocalRouter));
            LOGGER.info("register router success, find old local router={}, userId={}", oldLocalRouter, userId);
        }

        if (oldRemoteRouter != null && oldRemoteRouter.isOnline()) {
            EventBus.post(new RouterChangeEvent(userId, oldRemoteRouter));
            LOGGER.info("register router success, find old remote router={}, userId={}", oldRemoteRouter, userId);
        }

        return true;
    }

    /**
     * 解绑：先解除本地路由绑定，再接触远程路由绑定
     *
     * @param userId
     * @param clientType
     * @return
     */
    public boolean unRegister(String userId, int clientType) {
        localRouterManager.unregister(userId, clientType);
        remoteRouterManager.unregister(userId, clientType);
        return true;
    }

    /**
     * 查询路由：先查本地路由，本地没有再查远端路由
     *
     * @param userId
     * @param clientType
     * @return
     */
    public Router<?> lookup(String userId, int clientType) {
        LocalRouter localRouter = localRouterManager.lookup(userId, clientType);
        if (localRouter != null) {
            return localRouter;
        }
        RemoteRouter remoteRouter = remoteRouterManager.lookup(userId, clientType);
        return remoteRouter;
    }

}
