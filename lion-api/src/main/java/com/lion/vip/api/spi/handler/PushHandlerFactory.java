/**
 * FileName: PushHandlerFactory
 * Author:   Ren Xiaotian
 * Date:     2018/11/22 10:19
 */

package com.lion.vip.api.spi.handler;

import com.lion.vip.api.message.MessageHandler;
import com.lion.vip.api.spi.Factory;
import com.lion.vip.api.spi.SpiLoader;

/**
 * 接口：推送消息工厂
 */
public interface PushHandlerFactory extends Factory<MessageHandler> {
    static MessageHandler create() {
        return SpiLoader.load(PushHandlerFactory.class).get();
    }
}
