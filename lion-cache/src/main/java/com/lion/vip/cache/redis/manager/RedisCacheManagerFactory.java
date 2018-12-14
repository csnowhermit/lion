package com.lion.vip.cache.redis.manager;

import com.lion.vip.api.spi.Spi;
import com.lion.vip.api.spi.common.CacheManager;
import com.lion.vip.api.spi.common.CacheManagerFactory;

/**
 * Redis缓存管理工厂类
 */
@Spi(order = 1)
public class RedisCacheManagerFactory implements CacheManagerFactory {

    @Override
    public CacheManager get() {
        return RedisManager.I;
    }
}
