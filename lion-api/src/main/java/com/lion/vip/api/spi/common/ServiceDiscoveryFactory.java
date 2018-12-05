package com.lion.vip.api.spi.common;

import com.lion.vip.api.spi.Factory;
import com.lion.vip.api.spi.SpiLoader;
import com.lion.vip.api.srd.ServiceDiscovery;

/**
 * 接口：服务发现工厂
 */
public interface ServiceDiscoveryFactory extends Factory<ServiceDiscovery> {
    static ServiceDiscovery create() {
        return SpiLoader.load(ServiceDiscoveryFactory.class).get();
    }

}
