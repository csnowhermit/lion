package com.lion.vip.api.spi.core;

import com.lion.vip.api.connection.Cipher;
import com.lion.vip.api.spi.Factory;
import com.lion.vip.api.spi.SpiLoader;

/**
 * RSA加解密算法：工厂类
 */
public interface RSACipherFactory extends Factory<RSACipherFactory> {
    static Cipher create() {
        return SpiLoader.load(RSACipherFactory.class).get();
    }
}
