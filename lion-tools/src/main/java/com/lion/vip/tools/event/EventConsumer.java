package com.lion.vip.tools.event;

public abstract class EventConsumer {

    public EventConsumer() {
        EventBus.register(this);
    }
}
