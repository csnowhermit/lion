package com.lion.vip.common.router;

import com.lion.vip.api.router.ClientClassifier;
import com.lion.vip.api.spi.Spi;
import com.lion.vip.api.spi.router.ClientClassifierFactory;

@Spi(order = 1)
public final class DefaultClientClassifier implements ClientClassifier, ClientClassifierFactory {

    @Override
    public int getClientType(String osName) {
        return ClientType.find(osName).type;
    }

    @Override
    public ClientClassifier get() {
        return this;
    }
}
