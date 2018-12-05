package com.lion.vip.api.spi.client;

import com.lion.vip.api.spi.Factory;
import com.lion.vip.api.spi.SpiLoader;

public interface PusherFactory extends Factory<PusherFactory> {
    static PusherFactory create() {
        return SpiLoader.load(PusherFactory.class).get();
    }
}
