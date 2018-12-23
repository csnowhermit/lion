package com.lion.vip.api.spi.client;

import com.lion.vip.api.push.PushSender;
import com.lion.vip.api.spi.Factory;
import com.lion.vip.api.spi.SpiLoader;

public interface PusherFactory extends Factory<PushSender> {
    static PushSender create() {
        return SpiLoader.load(PusherFactory.class).get();
    }
}
