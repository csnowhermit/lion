/**
 * FileName: Plugin
 * Author:   Ren Xiaotian
 * Date:     2018/11/21 17:13
 */

package com.lion.vip.api.spi;

import com.lion.vip.api.LionContext;

public interface Plugin {
    default void init(LionContext context) {
    }

    default void destroy() {
    }

}
