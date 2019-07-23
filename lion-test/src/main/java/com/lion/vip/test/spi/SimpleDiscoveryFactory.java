
package com.lion.vip.test.spi;

import com.lion.vip.api.spi.Spi;
import com.lion.vip.api.spi.common.ServiceDiscoveryFactory;
import com.lion.vip.api.srd.ServiceDiscovery;

/**
 */
@Spi(order = 2)
public final class SimpleDiscoveryFactory implements ServiceDiscoveryFactory {
    @Override
    public ServiceDiscovery get() {
        return FileSrd.I;
    }
}
