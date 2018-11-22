/**
 * FileName: ServerEventListenerFactory
 * Author:   Ren Xiaotian
 * Date:     2018/11/22 9:57
 */

package com.lion.vip.api.spi.core;

import com.lion.vip.api.common.ServerEventListener;
import com.lion.vip.api.spi.Factory;
import com.lion.vip.api.spi.SpiLoader;

/**
 * 接口：服务事件监听工厂
 */
public interface ServerEventListenerFactory extends Factory<ServerEventListener> {
    static ServerEventListener create() {
        return SpiLoader.load(ServerEventListenerFactory.class).get();
    }
}
