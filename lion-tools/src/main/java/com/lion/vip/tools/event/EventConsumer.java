/**
 * FileName: EventConsumer
 * Author:   Ren Xiaotian
 * Date:     2018/11/22 16:34
 */

package com.lion.vip.tools.event;

public abstract class EventConsumer {

    public EventConsumer() {
        EventBus.register(this);
    }
}
