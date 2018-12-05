package com.lion.vip.api.spi.router;

import com.lion.vip.api.router.ClientClassifier;
import com.lion.vip.api.spi.Factory;
import com.lion.vip.api.spi.SpiLoader;

public interface ClientClassifierFactory extends Factory<ClientClassifier> {

    static ClientClassifier create() {
        return SpiLoader.load(ClientClassifierFactory.class).get();
    }
}
