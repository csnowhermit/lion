package com.lion.vip.api;

import com.lion.vip.api.common.Monitor;
import com.lion.vip.api.spi.common.CacheManager;
import com.lion.vip.api.spi.common.MQClient;
import com.lion.vip.api.srd.ServiceDiscovery;
import com.lion.vip.api.srd.ServiceRegistry;

public interface LionContext {

    Monitor getMonitor();

    ServiceDiscovery getDiscovery();

    ServiceRegistry getRegistry();

    CacheManager getCacheManager();

    MQClient getMQClient();

}
