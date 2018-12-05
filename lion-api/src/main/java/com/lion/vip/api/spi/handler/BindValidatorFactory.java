package com.lion.vip.api.spi.handler;

import com.lion.vip.api.spi.Factory;
import com.lion.vip.api.spi.SpiLoader;

/**
 * 接口：绑定对象工厂
 */
public interface BindValidatorFactory extends Factory<BindValidator> {
    static BindValidator create(){
        return SpiLoader.load(BindValidatorFactory.class).get();
    }
}
