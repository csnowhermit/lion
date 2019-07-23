
package com.lion.vip.test.spi;

import com.lion.vip.api.spi.Spi;
import com.lion.vip.api.spi.common.CacheManager;
import com.lion.vip.api.spi.common.CacheManagerFactory;

/**
 */
@Spi(order = 2)
public final class SimpleCacheMangerFactory implements CacheManagerFactory {
    @Override
    public CacheManager get() {
        return FileCacheManger.I;
    }
}
