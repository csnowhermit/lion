/**
 * FileName: MQClientFactory
 * Author:   Ren Xiaotian
 * Date:     2018/11/22 9:40
 */

package com.lion.vip.api.spi.common;

import com.lion.vip.api.spi.Factory;
import com.lion.vip.api.spi.SpiLoader;

public interface MQClientFactory extends Factory<MQClient> {
    static MQClient create() {
        return SpiLoader.load(MQClientFactory.class).get();
    }
}
