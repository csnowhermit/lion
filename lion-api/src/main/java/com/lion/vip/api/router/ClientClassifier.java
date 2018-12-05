package com.lion.vip.api.router;

import com.lion.vip.api.spi.router.ClientClassifierFactory;

public interface ClientClassifier {
    ClientClassifier I = ClientClassifierFactory.create();

    int getClientType(String osName);
}
