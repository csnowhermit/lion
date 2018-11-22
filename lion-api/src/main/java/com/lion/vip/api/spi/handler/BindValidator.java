/**
 * FileName: BindValidator
 * Author:   Ren Xiaotian
 * Date:     2018/11/22 10:15
 */

package com.lion.vip.api.spi.handler;

import com.lion.vip.api.spi.Plugin;

/**
 * 用户绑定验证
 */
public interface BindValidator extends Plugin {
    boolean validate(String userId, String data);
}
