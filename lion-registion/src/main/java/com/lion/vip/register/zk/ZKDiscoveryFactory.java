package com.lion.vip.register.zk;

import com.lion.vip.api.spi.Spi;
import com.lion.vip.api.spi.common.ServiceDiscoveryFactory;
import com.lion.vip.api.srd.ServiceDiscovery;

/**
 * zk 服务发现工厂
 */
@Spi(order=1)
public class ZKDiscoveryFactory implements ServiceDiscoveryFactory {
    @Override
    public ServiceDiscovery get() {
        return ZKServiceRegistryAndDiscovery.I;
    }
}
