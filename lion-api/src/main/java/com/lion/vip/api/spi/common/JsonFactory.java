package com.lion.vip.api.spi.common;

import com.lion.vip.api.spi.Factory;
import com.lion.vip.api.spi.SpiLoader;

public interface JsonFactory extends Factory<Json> {
    static Json create() {
        return SpiLoader.load(JsonFactory.class).get();    //加载一个工厂，去生产类
    }
}
