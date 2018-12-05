package com.lion.vip.api.srd;

import com.lion.vip.api.service.Service;

/**
 * 服务节点注册
 */
public interface ServiceRegistry extends Service {

    /**
     * 注册节点
     * @param serviceNode
     */
    void register(ServiceNode serviceNode);

    /**
     * 解绑节点
     * @param serviceNode
     */
    void deregister(ServiceNode serviceNode);


}
