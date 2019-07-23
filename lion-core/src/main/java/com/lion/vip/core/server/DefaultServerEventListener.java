package com.lion.vip.core.server;

import com.lion.vip.api.common.ServerEventListener;
import com.lion.vip.api.spi.Spi;
import com.lion.vip.api.spi.core.ServerEventListenerFactory;

@Spi(order = 1)
public final class DefaultServerEventListener implements ServerEventListener, ServerEventListenerFactory {

    @Override
    public ServerEventListener get() {
        return this;
    }
}
