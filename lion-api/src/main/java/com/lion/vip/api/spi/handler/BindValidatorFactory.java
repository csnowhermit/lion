/**
 * FileName: BindValidatorFactory
 * Author:   Ren Xiaotian
 * Date:     2018/11/22 10:16
 */

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
