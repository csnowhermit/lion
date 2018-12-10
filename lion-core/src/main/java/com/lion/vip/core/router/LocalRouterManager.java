package com.lion.vip.core.router;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.lion.vip.api.connection.Connection;
import com.lion.vip.api.connection.SessionContext;
import com.lion.vip.api.event.ConnectionCloseEvent;
import com.lion.vip.api.event.UserOfflineEvent;
import com.lion.vip.api.router.RouterManager;
import com.lion.vip.tools.event.EventBus;
import com.lion.vip.tools.event.EventConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地路由管理器
 */
public final class LocalRouterManager extends EventConsumer implements RouterManager<LocalRouter> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalRouterManager.class);
    private static final Map<Integer, LocalRouter> EMPTY = new HashMap<>(0);

    //本地路由表
    private final Map<String, Map<Integer, LocalRouter>> routerMap = new ConcurrentHashMap<>();


    @Override
    public LocalRouter register(String userId, LocalRouter router) {
        LOGGER.info("register local router success userId={}, router={}", userId, router);
        return routerMap.computeIfAbsent(userId, s -> new HashMap<>(1)).put((int) router.getClientType(), router);
    }

    @Override
    public boolean unregister(String userId, int clientType) {
        LocalRouter localRouter = routerMap.getOrDefault(userId, EMPTY).remove(clientType);
        LOGGER.info("unregister local router success userId={}, router={}", userId, localRouter);
        return true;
    }

    @Override
    public Set<LocalRouter> lookupAll(String userId) {
        return new HashSet<>(routerMap.getOrDefault(userId, EMPTY).values());
    }

    @Override
    public LocalRouter lookup(String userId, int clientType) {
        LocalRouter localRouter = routerMap.getOrDefault(userId, EMPTY).get(clientType);
        LOGGER.info("lookup local router userId={}, router={}", userId, localRouter);
        return localRouter;
    }

    public Map routers() {
        return routerMap;
    }

    /**
     * 建通连接关闭事件，清掉无用的路由
     *
     * @param event
     */
    @Subscribe
    @AllowConcurrentEvents
    void on(ConnectionCloseEvent event) {
        Connection connection = event.connection;
        if (connection == null) {
            return;
        }

        SessionContext context = connection.getSessionContext();
        String userId = context.userId;
        if (userId == null) {
            return;
        }

        int clientType = context.getClientType();
        LocalRouter localRouter = routerMap.getOrDefault(userId, EMPTY).get(clientType);
        if (localRouter == null) {
            return;
        }

        String connId = connection.getId();
        //1.检测下是否为同一连接，如果是客户端重连，则老的路由会被新的路由覆盖
        if (connId.equals(localRouter.getRouteValue().getId())) {
            //2.是同一路由的话，删除路由
            routerMap.getOrDefault(userId, EMPTY).remove(clientType);

            //3.发送用户下线事件，只有老路由存在的情况下才会发送，因为有可能用户重连了，而老的连接又是在新连接之后断开
            //这时候会出问题，会导致用户变成下线状态，实际上用户是在线的
            EventBus.post(new UserOfflineEvent(event.connection, userId));
            LOGGER.info("clean disconnected local route, userId={}, route={}", userId, localRouter);
        } else {    //如果不想等，说明不是同一连接
            LOGGER.info("clean disconnected local route, not clean: userId={}, route={}", userId, localRouter);
        }

    }

}
