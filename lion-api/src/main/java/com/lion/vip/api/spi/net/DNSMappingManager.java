/**
 * FileName: DNSMappingManager
 * Author:   Ren Xiaotian
 * Date:     2018/11/22 10:38
 */

package com.lion.vip.api.spi.net;

import com.lion.vip.api.service.Service;
import com.lion.vip.api.spi.SpiLoader;

/**
 * DNS映射关系管理器
 */
public interface DNSMappingManager extends Service {
    static DNSMappingManager create() {
        return SpiLoader.load(DNSMappingManager.class);
    }

    DNSMapping lookup(String origin);
}
