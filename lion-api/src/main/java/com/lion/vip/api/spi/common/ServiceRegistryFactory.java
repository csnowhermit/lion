package com.lion.vip.api.spi.common;

import com.lion.vip.api.spi.Factory;
import com.lion.vip.api.spi.SpiLoader;
import com.lion.vip.api.srd.ServiceRegistry;

/**
 * 接口：服务注册工厂
 */
public interface ServiceRegistryFactory extends Factory<ServiceRegistry> {
    static ServiceRegistry create() {
        return SpiLoader.load(ServiceRegistryFactory.class).get();
    }
}
