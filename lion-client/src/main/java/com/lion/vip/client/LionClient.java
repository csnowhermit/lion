package com.lion.vip.client;

import com.lion.vip.api.LionContext;
import com.lion.vip.api.common.Monitor;
import com.lion.vip.api.spi.common.*;
import com.lion.vip.api.srd.ServiceDiscovery;
import com.lion.vip.api.srd.ServiceRegistry;
import com.lion.vip.client.push.PushRequestBus;
import com.lion.vip.common.message.PushMessage;
import com.lion.vip.common.router.CachedRemoteRouterManager;
import com.lion.vip.monitor.service.MonitorService;
import com.lion.vip.monitor.service.ThreadPoolManager;
import com.lion.vip.tools.event.EventBus;

public final class LionClient implements LionContext {
    private MonitorService monitorService;    //监控服务
    private PushRequestBus pushRequestBus;
    private CachedRemoteRouterManager cachedRemoteRouterManager;    //缓存 远程路由管理器
    private GatewayConnectionFactory gatewayConnectionFactory;      //网关连接工厂


    public LionClient() {
        this.monitorService = new MonitorService();

        EventBus.create(monitorService.getThreadPoolManager().getEventBusExecutor());

        this.pushRequestBus = new PushRequestBus(this);
        this.cachedRemoteRouterManager = new CachedRemoteRouterManager();
        this.gatewayConnectionFactory = GatewayConnectionFactory.create(this);
    }

    public ThreadPoolManager getThreadPoolManager() {
        return monitorService.getThreadPoolManager();
    }

    public MonitorService getMonitorService() {
        return monitorService;
    }

    public PushRequestBus getPushRequestBus() {
        return pushRequestBus;
    }

    public CachedRemoteRouterManager getCachedRemoteRouterManager() {
        return cachedRemoteRouterManager;
    }

    public GatewayConnectionFactory getGatewayConnectionFactory() {
        return gatewayConnectionFactory;
    }

    @Override
    public Monitor getMonitor() {
        return monitorService;
    }


    @Override
    public ServiceDiscovery getDiscovery() {
        return ServiceDiscoveryFactory.create();
    }

    @Override
    public ServiceRegistry getRegistry() {
        return ServiceRegistryFactory.create();
    }

    @Override
    public CacheManager getCacheManager() {
        return CacheManagerFactory.create();
    }

    @Override
    public MQClient getMQClient() {
        return MQClientFactory.create();
    }
}
