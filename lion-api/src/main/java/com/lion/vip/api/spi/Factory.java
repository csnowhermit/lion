/**
 * FileName: Factory
 * Author:   Ren Xiaotian
 * Date:     2018/11/21 17:11
 */

package com.lion.vip.api.spi;

import java.util.function.Supplier;

public interface Factory<T> extends Supplier<T> {
}
