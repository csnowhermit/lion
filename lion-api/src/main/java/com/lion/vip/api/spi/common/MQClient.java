/**
 * FileName: MQClient
 * Author:   Ren Xiaotian
 * Date:     2018/11/22 9:36
 */

package com.lion.vip.api.spi.common;

import com.lion.vip.api.spi.Plugin;

/**
 * 消息处理客户端接口
 */
public interface MQClient extends Plugin {

    /**
     * 订阅topic的消息
     *
     * @param topic             主题
     * @param mqMessageReceiver 消息接收者
     */
    void subscribe(String topic, MQMessageReceiver mqMessageReceiver);

    /**
     * 发布消息到消息队列
     *
     * @param topic   主题
     * @param message 消息的内容
     */
    void publish(String topic, Object message);

}
