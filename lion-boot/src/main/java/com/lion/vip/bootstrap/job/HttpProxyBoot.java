package com.lion.vip.bootstrap.job;

import com.lion.vip.api.spi.net.DNSMappingManager;
import com.lion.vip.core.LionServer;

/**
 * http代理启动器
 */
public final class HttpProxyBoot extends BootJob {
    private final LionServer lionServer;

    public HttpProxyBoot(LionServer lionServer) {
        this.lionServer = lionServer;
    }

    @Override
    protected void start() {
        lionServer.getHttpClient().syncStart();
        DNSMappingManager.create().start();
        startNext();
    }

    @Override
    protected void stop() {
        stopNext();
        lionServer.getHttpClient().syncStop();
        DNSMappingManager.create().stop();

    }
}
