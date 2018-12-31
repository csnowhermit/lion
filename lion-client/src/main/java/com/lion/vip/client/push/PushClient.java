package com.lion.vip.client.push;

import com.lion.vip.api.LionContext;
import com.lion.vip.api.push.PushContext;
import com.lion.vip.api.push.PushException;
import com.lion.vip.api.push.PushResult;
import com.lion.vip.api.push.PushSender;
import com.lion.vip.api.service.BaseService;
import com.lion.vip.api.service.Listener;
import com.lion.vip.api.spi.common.CacheManagerFactory;
import com.lion.vip.api.spi.common.ServiceDiscoveryFactory;
import com.lion.vip.client.LionClient;
import com.lion.vip.client.gateway.connection.GatewayConnectionFactory;
import com.lion.vip.common.router.CachedRemoteRouterManager;
import com.lion.vip.common.router.RemoteRouter;

import java.util.Set;
import java.util.concurrent.FutureTask;

public final class PushClient extends BaseService implements PushSender {
    private LionClient lionClient;
    private PushRequestBus pushRequestBus;
    private CachedRemoteRouterManager cachedRemoteRouterManager;
    private GatewayConnectionFactory gatewayConnectionFactory;

    @Override
    public FutureTask<PushResult> send(PushContext ctx) {
        if (ctx.isBroadcast()) {
            return send0(ctx.setUserId(null));
        } else if (ctx.getUserId() != null) {
            return send0(ctx);
        } else if (ctx.getUserIds() != null) {
            FutureTask<PushResult> task = null;
            for (String userId : ctx.getUserIds()) {
                task = send0(ctx.setUserId(userId));
            }
            return task;
        } else {
            throw new PushException("param error.");
        }
    }

    private FutureTask<PushResult> send0(PushContext ctx) {
        if (ctx.isBroadcast()) {
            return PushRequest.build(lionClient, ctx).broadcast();
        } else {
            Set<RemoteRouter> remoteRouterSet = cachedRemoteRouterManager.lookupAll(ctx.getUserId());
            if (remoteRouterSet == null || remoteRouterSet.isEmpty()) {
                return PushRequest.build(lionClient, ctx).onOffline();
            }

            FutureTask<PushResult> task = null;

            for (RemoteRouter remoteRouter : remoteRouterSet) {
                task = PushRequest.build(lionClient, ctx).send(remoteRouter);
            }
            return task;
        }
    }

    @Override
    protected void doStart(Listener listener) throws Throwable {
        if (lionClient == null) {
            lionClient = new LionClient();
        }

        pushRequestBus = lionClient.getPushRequestBus();
        cachedRemoteRouterManager = lionClient.getCachedRemoteRouterManager();
        gatewayConnectionFactory = lionClient.getGatewayConnectionFactory();

        ServiceDiscoveryFactory.create().syncStart();
        CacheManagerFactory.create().init();
        pushRequestBus.syncStart();
        gatewayConnectionFactory.start(listener);
    }

    @Override
    protected void doStop(Listener listener) throws Throwable {
        ServiceDiscoveryFactory.create().syncStop();
        CacheManagerFactory.create().destroy();
        pushRequestBus.syncStop();
        gatewayConnectionFactory.stop(listener);
    }

    @Override
    public boolean isRunning() {
        return started.get();
    }

    @Override
    public void setLionContext(LionContext lionContext) {
        this.lionClient = (LionClient) lionContext;
    }
}
