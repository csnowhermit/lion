package com.lion.vip.core;

import com.lion.vip.api.LionContext;
import com.lion.vip.api.common.Monitor;
import com.lion.vip.api.spi.common.*;
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
    private GatewayUDPConnector udpGatewayServer;

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
            this.udpGatewayServer = new GatewayUDPConnector(this);
        }
    }

    public ServiceNode getConnServerNode() {
        return connServerNode;
    }

    public void setConnServerNode(ServiceNode connServerNode) {
        this.connServerNode = connServerNode;
    }

    public ServiceNode getGatewayServerNode() {
        return gatewayServerNode;
    }

    public void setGatewayServerNode(ServiceNode gatewayServerNode) {
        this.gatewayServerNode = gatewayServerNode;
    }

    public ServiceNode getWebsocketServerNode() {
        return websocketServerNode;
    }

    public void setWebsocketServerNode(ServiceNode websocketServerNode) {
        this.websocketServerNode = websocketServerNode;
    }

    public ConnectionServer getConnectionServer() {
        return connectionServer;
    }

    public void setConnectionServer(ConnectionServer connectionServer) {
        this.connectionServer = connectionServer;
    }

    public WebsocketServer getWebsocketServer() {
        return websocketServer;
    }

    public void setWebsocketServer(WebsocketServer websocketServer) {
        this.websocketServer = websocketServer;
    }

    public GatewayServer getGatewayServer() {
        return gatewayServer;
    }

    public void setGatewayServer(GatewayServer gatewayServer) {
        this.gatewayServer = gatewayServer;
    }

    public AdminServer getAdminServer() {
        return adminServer;
    }

    public void setAdminServer(AdminServer adminServer) {
        this.adminServer = adminServer;
    }

    public GatewayUDPConnector getUdpGatewayServer() {
        return udpGatewayServer;
    }

    public void setUdpGatewayServer(GatewayUDPConnector udpGatewayServer) {
        this.udpGatewayServer = udpGatewayServer;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public PushCenter getPushCenter() {
        return pushCenter;
    }

    public void setPushCenter(PushCenter pushCenter) {
        this.pushCenter = pushCenter;
    }

    public ReusableSessionManager getReusableSessionManager() {
        return reusableSessionManager;
    }

    public void setReusableSessionManager(ReusableSessionManager reusableSessionManager) {
        this.reusableSessionManager = reusableSessionManager;
    }

    public RouterCenter getRouterCenter() {
        return routerCenter;
    }

    public void setRouterCenter(RouterCenter routerCenter) {
        this.routerCenter = routerCenter;
    }

    public MonitorService getMonitorService() {
        return monitorService;
    }

    public void setMonitorService(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @Override
    public MonitorService getMonitor() {
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


    /**
     * 判断本机是不是目标机器
     *
     * @param host
     * @param port
     * @return
     */
    public boolean isTargetMachine(String host, int port) {
        return port == gatewayServerNode.getPort() && gatewayServerNode.getHost().equals(host);
    }
}
