/**
 * FileName: ServiceDiscovery
 * Author:   Ren Xiaotian
 * Date:     2018/11/21 17:08
 */

package com.lion.vip.api.srd;

import com.lion.vip.api.service.Service;

import java.util.List;

/**
 * 服务发现
 */
public interface ServiceDiscovery extends Service {

    List<ServiceNode> lookup(String path);

    void subscribe(String path, ServiceListener serviceListener);

    void unsubscribe(String path, ServiceListener serviceListener);

}
