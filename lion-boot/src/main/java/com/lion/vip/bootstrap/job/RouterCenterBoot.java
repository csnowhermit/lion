package com.lion.vip.bootstrap.job;

import com.lion.vip.core.LionServer;

/**
 * 路由中心启动器
 */
public final class RouterCenterBoot extends BootJob {
    private final LionServer lionServer;

    public RouterCenterBoot(LionServer lionServer) {
        this.lionServer = lionServer;
    }

    @Override
    protected void start() {
        lionServer.getRouterCenter().start();
        startNext();
    }

    @Override
    protected void stop() {
        stopNext();
        lionServer.getRouterCenter().stop();
    }
}
