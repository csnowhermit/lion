package com.lion.vip.bootstrap.job;

import com.lion.vip.api.service.Listener;
import com.lion.vip.api.service.Server;
import com.lion.vip.api.spi.common.ServiceDiscoveryFactory;
import com.lion.vip.api.spi.common.ServiceRegistryFactory;
import com.lion.vip.api.srd.ServiceNode;
import com.lion.vip.tools.log.Logs;

/**
 * 需要分布式部署的服务的启动器：Client接入服务、websocket接入服务、TCP网关服务、UDP网关服务
 */
public final class ServerBoot extends BootJob {
    private final Server server;      //服务
    private final ServiceNode node;   //节点

    public ServerBoot(Server server, ServiceNode node) {
        this.server = server;
        this.node = node;
    }

    @Override
    protected void start() {
        server.init();
        server.start(new Listener() {
            @Override
            public void onSuccess(Object... args) {
                Logs.Console.info("start {} success on: {}", server.getClass().getSimpleName(), args[0]);
                if (node != null) {    //注册应用到zk
                    ServiceRegistryFactory.create().register(node);
                    Logs.RSD.info("register {} to srd success", node);
                }
                startNext();
            }

            @Override
            public void onFailure(Throwable cause) {
                Logs.Console.info("start {} failure, JVM exit with code -1", server.getClass().getSimpleName());
                System.exit(-1);
            }
        });
    }

    @Override
    protected void stop() {
        stopNext();
        if (node != null) {
            ServiceRegistryFactory.create().deregister(node);    //服务的反注册
        }

        Logs.Console.info("try shutdown {} ...", server.getClass().getSimpleName());
        server.stop().join();    //确保服务逐个关闭
        Logs.Console.info("{} shutdown success.", server.getClass().getSimpleName());
    }

    @Override
    protected String getName() {
        return super.getName() + "( " + server.getClass().getName() + " )";
    }
}
