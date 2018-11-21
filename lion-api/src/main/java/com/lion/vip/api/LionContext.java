/**
 * FileName: LionContext
 * Author:   Ren Xiaotian
 * Date:     2018/11/21 16:18
 */

package com.lion.vip.api;

import com.lion.vip.api.common.Monitor;

public interface LionContext {

    Monitor getMonitor();

    ServiceDiscovery getDiscovery();

    ServiceRegistry getRegistry();

    CacheManager getCacheManager();

    MQClient getMQClient();

}
