package com.lion.vip.core;

import com.lion.vip.api.LionContext;
import com.lion.vip.api.common.Monitor;
import com.lion.vip.api.spi.common.CacheManager;
import com.lion.vip.api.spi.common.MQClient;
import com.lion.vip.api.srd.ServiceDiscovery;
import com.lion.vip.api.srd.ServiceNode;
import com.lion.vip.api.srd.ServiceRegistry;
import com.lion.vip.common.ServiceNodes;
import com.lion.vip.core.push.PushCenter;
import com.lion.vip.core.router.RouterCenter;
import com.lion.vip.core.server.*;
import com.lion.vip.core.session.ReusableSessionManager;
import com.lion.vip.monitor.service.MonitorService;
import com.lion.vip.network.netty.http.HttpClient;
import com.lion.vip.tools.event.EventBus;

import static com.lion.vip.tools.config.CC.lion.net.tcpGateway;

/**
 * Lion 服务器程序
 */
public class LionServer implements LionContext {
    private ServiceNode connServerNode;           //连接服务器节点
    private ServiceNode gatewayServerNode;        //网关服务器节点
    private ServiceNode websocketServerNode;      //websocket服务器节点：用websocket client来模拟客户端测试

    public ConnectionServer connectionServer;     //连接服务器
    public WebsocketServer websocketServer;       //websocket服务器
    private GatewayServer gatewayServer;          //网关服务器
    private AdminServer adminServer;              //后台管理员服务器
    private GatewayUDPConnector gatewayUDPConnector;

    private HttpClient httpClient;     //http客户端
    private PushCenter pushCenter;     //推送中心
    private ReusableSessionManager reusableSessionManager;    //可重复使用的session管理器
    private RouterCenter routerCenter;      //路由中心
    private MonitorService monitorService;  //监控服务

    public LionServer() {
        this.connServerNode = ServiceNodes.cs();
        this.gatewayServerNode = ServiceNodes.gs();
        this.websocketServerNode = ServiceNodes.ws();

        this.monitorService = new MonitorService();
        EventBus.create(this.monitorService.getThreadPoolManager().getEventBusExecutor());

        this.reusableSessionManager = new ReusableSessionManager();
        this.pushCenter = new PushCenter(this);
        this.routerCenter = new RouterCenter(this);
        this.connectionServer = new ConnectionServer(this);
        this.websocketServer = new WebsocketServer(this);
        this.adminServer = new AdminServer(this);

        if (tcpGateway()) {
            this.gatewayServer = new GatewayServer(this);
        } else {
            this.gatewayUDPConnector = new GatewayUDPConnector(this);
        }
    }

    @Override
    public Monitor getMonitor() {
        return null;
    }

    @Override
    public ServiceDiscovery getDiscovery() {
        return null;
    }

    @Override
    public ServiceRegistry getRegistry() {
        return null;
    }

    @Override
    public CacheManager getCacheManager() {
        return null;
    }

    @Override
    public MQClient getMQClient() {
        return null;
    }


}
