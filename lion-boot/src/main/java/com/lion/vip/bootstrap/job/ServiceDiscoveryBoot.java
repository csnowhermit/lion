package com.lion.vip.bootstrap.job;

import com.lion.vip.api.spi.common.ServiceDiscoveryFactory;
import com.lion.vip.tools.log.Logs;

/**
 * 服务发现启动器
 */
public final class ServiceDiscoveryBoot extends BootJob {
    @Override
    protected void start() {
        Logs.Console.info("init service discovery waiting for connected...");
        ServiceDiscoveryFactory.create().syncStart();
        startNext();
    }

    @Override
    protected void stop() {
        stopNext();
        ServiceDiscoveryFactory.create().syncStop();
        Logs.Console.info("service discovery closed...");
    }
}
