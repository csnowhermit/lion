package com.lion.vip.bootstrap.job;

import com.lion.vip.core.LionServer;

/**
 * 推送中心启动器
 */
public final class PushCenterBoot extends BootJob {
    private final LionServer lionServer;

    public PushCenterBoot(LionServer lionServer) {
        this.lionServer = lionServer;
    }

    @Override
    protected void start() {
        lionServer.getPushCenter().start();
        startNext();
    }

    @Override
    protected void stop() {
        stopNext();
        lionServer.getPushCenter().stop();
    }
}
