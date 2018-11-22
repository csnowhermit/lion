/**
 * FileName: PushListenerFactory
 * Author:   Ren Xiaotian
 * Date:     2018/11/22 10:45
 */

package com.lion.vip.api.spi.push;

import com.lion.vip.api.spi.Factory;
import com.lion.vip.api.spi.SpiLoader;

/**
 * 接口：消息推送监听者工厂
 */
public interface PushListenerFactory<T extends IPushMessage> extends Factory<PushListener<T>> {

    @SuppressWarnings("unchecked")
    static <T extends IPushMessage> PushListener<T> create(){
        return (PushListener<T>) SpiLoader.load(PushListenerFactory.class).get();
    }
}
