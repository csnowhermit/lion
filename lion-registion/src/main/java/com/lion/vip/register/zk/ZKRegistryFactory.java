package com.lion.vip.register.zk;

import com.lion.vip.api.spi.Spi;
import com.lion.vip.api.spi.common.ServiceRegistryFactory;
import com.lion.vip.api.srd.ServiceRegistry;

@Spi(order = 1)
public class ZKRegistryFactory implements ServiceRegistryFactory {
    @Override
    public ServiceRegistry get() {
        return ZKServiceRegistryAndDiscovery.I;
    }
}
