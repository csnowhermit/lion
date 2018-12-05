package com.lion.vip.api.spi.common;

import com.lion.vip.api.spi.Factory;
import com.lion.vip.api.spi.SpiLoader;

public interface MQClientFactory extends Factory<MQClient> {
    static MQClient create() {
        return SpiLoader.load(MQClientFactory.class).get();
    }
}
