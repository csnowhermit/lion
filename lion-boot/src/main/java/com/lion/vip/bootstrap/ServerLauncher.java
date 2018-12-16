package com.lion.vip.bootstrap;

import com.lion.vip.api.common.ServerEventListener;
import com.lion.vip.api.spi.common.CacheManager;
import com.lion.vip.api.spi.core.ServerEventListenerFactory;
import com.lion.vip.api.srd.ServiceDiscovery;
import com.lion.vip.api.srd.ServiceRegistry;
import com.lion.vip.bootstrap.job.BootChain;
import com.lion.vip.core.LionServer;
import com.lion.vip.tools.config.CC;

/**
 * 服务启动类
 */
public final class ServerLauncher {
    private LionServer lionServer;
    private BootChain bootChain;
    private ServerEventListener serverEventListener;

    public LionServer getLionServer() {
        return lionServer;
    }

    public void setLionServer(LionServer lionServer) {
        this.lionServer = lionServer;
    }

    public BootChain getBootChain() {
        return bootChain;
    }

    public void setBootChain(BootChain bootChain) {
        this.bootChain = bootChain;
    }

    public ServerEventListener getServerEventListener() {
        return serverEventListener;
    }

    public void setServerEventListener(ServerEventListener serverEventListener) {
        this.serverEventListener = serverEventListener;
    }

    /**
     * Server初始化
     */
    public void init() {
        if (lionServer == null) {
            this.lionServer = new LionServer();
        }

        if (bootChain == null) {
            this.bootChain = BootChain.chain();
        }

        if (serverEventListener == null) {
            this.serverEventListener = ServerEventListenerFactory.create();
        }

        this.serverEventListener.init(lionServer);

        bootChain.boot()
                .setNext(new CacheManagerBoot())          //1.启动缓存服务
                .setNext(new ServiceRegistryBoot())       //2.启动服务注册
                .setNext(new ServiceDiscoveryBoot())      //3.启动服务发现
                .setNext(new ServerBoot(lionServer.getConnectionServer(), lionServer.getConnServerNode()))              //4.启动介入服务
                .setNext(() -> new ServerBoot(lionServer.getWebsocketServer(), lionServer.getWebsocketServerNode()))    //5.启动WebSocket接入服务
                .setNext(() -> new ServerBoot(lionServer.getUdpGatewayServer(), lionServer.getGatewayServerNode()))     //6.启动UDP网关服务
                .setNext(() -> new ServerBoot(lionServer.getGatewayServer(), lionServer.getGatewayServerNode()))        //7.启动TCP网关服务
                .setNext(new ServerBoot(lionServer.getAdminServer(), null))      //8.启动控制台服务
                .setNext(new RouterCenterBoot(lionServer))       //9.启动路由中心
                .setNext(new PushCenterBoot(lionServer))         //10.启动推送中心
                .setNext(() -> new HttpProxyBoot(lionServer), CC.lion.http.proxy_enabled)    //11.启动http代理服务，DNS解析服务
                .setNext(new MonitorBoot(lionServer))            //12.启动监控服务
                .end();
    }

    /**
     * Server启动
     */
    public void start() {
        bootChain.start();
    }

    /**
     * Server停止
     */
    public void stop() {
        bootChain.stop();
    }
}
