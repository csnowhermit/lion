package com.lion.vip.api.spi.handler;

import com.lion.vip.api.spi.Plugin;

/**
 * 用户绑定验证
 */
public interface BindValidator extends Plugin {
    boolean validate(String userId, String data);
}
