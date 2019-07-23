
package com.lion.vip.test.spi;

import com.lion.vip.api.spi.Spi;
import com.lion.vip.api.spi.common.ServiceRegistryFactory;
import com.lion.vip.api.srd.ServiceRegistry;

/**
 */
@Spi(order = 2)
public final class SimpleRegistryFactory implements ServiceRegistryFactory {
    @Override
    public ServiceRegistry get() {
        return FileSrd.I;
    }
}
