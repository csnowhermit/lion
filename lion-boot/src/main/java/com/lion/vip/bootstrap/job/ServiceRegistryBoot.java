package com.lion.vip.bootstrap.job;

import com.lion.vip.api.spi.common.ServiceRegistryFactory;
import com.lion.vip.tools.log.Logs;

/**
 * 服务注册启动器
 */
public final class ServiceRegistryBoot extends BootJob {
    @Override
    protected void start() {
        Logs.Console.info("init service registry waiting for connected...");
        ServiceRegistryFactory.create().init();
        startNext();
    }

    @Override
    protected void stop() {
        stopNext();
        ServiceRegistryFactory.create().syncStop();
        Logs.Console.info("service registry closed...");
    }
}
