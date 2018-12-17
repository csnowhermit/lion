package com.lion.vip.bootstrap.job;

import com.lion.vip.api.spi.common.CacheManagerFactory;

/**
 * 缓存管理启动器
 */
public final class CacheManagerBoot extends BootJob {
    @Override
    protected void start() {
        CacheManagerFactory.create().init();
        startNext();
    }

    @Override
    protected void stop() {
        stopNext();
        CacheManagerFactory.create().destroy();
    }
}
