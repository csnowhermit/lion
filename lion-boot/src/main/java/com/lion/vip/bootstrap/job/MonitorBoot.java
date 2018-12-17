package com.lion.vip.bootstrap.job;

import com.lion.vip.core.LionServer;

/**
 * 监控模块启动器
 */
public final class MonitorBoot extends BootJob {
    private final LionServer lionServer;

    public MonitorBoot(LionServer lionServer) {
        this.lionServer = lionServer;
    }

    @Override
    protected void start() {
        lionServer.getMonitor().start();
        startNext();
    }

    @Override
    protected void stop() {
        stopNext();
        lionServer.getMonitor().stop();
    }
}
