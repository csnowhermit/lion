package com.lion.vip.api.spi.common;

import com.lion.vip.api.spi.Factory;
import com.lion.vip.api.spi.SpiLoader;

public interface CacheManagerFactory extends Factory<CacheManager> {
    static CacheManager create() {
        return SpiLoader.load(CacheManagerFactory.class).get();
    }
}
