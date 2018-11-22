/**
 * FileName: MessagePusher
 * Author:   Ren Xiaotian
 * Date:     2018/11/22 10:42
 */

package com.lion.vip.api.spi.push;

/**
 * 消息推送
 */
public interface MessagePusher {

    void push(IPushMessage message);
}
