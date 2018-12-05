package com.lion.vip.api.spi;

import com.lion.vip.api.LionContext;

public interface Plugin {
    default void init(LionContext context) {
    }

    default void destroy() {
    }

}
