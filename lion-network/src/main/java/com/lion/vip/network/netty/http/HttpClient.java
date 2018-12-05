package com.lion.vip.network.netty.http;

import com.lion.vip.api.service.Service;

/**
 * http 客户端
 */
public interface HttpClient extends Service {
    void request(RequestContext context) throws Exception;

}
