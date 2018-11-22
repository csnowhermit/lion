/**
 * FileName: MQMessageReceiver
 * Author:   Ren Xiaotian
 * Date:     2018/11/22 9:42
 */

package com.lion.vip.api.spi.common;

/**
 * 消息队列 消息接收者
 */
public interface MQMessageReceiver {
    void receive(String topic, Object message);
}
