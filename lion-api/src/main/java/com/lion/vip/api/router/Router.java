/**
 * FileName: Router
 * Author:   Ren Xiaotian
 * Date:     2018/11/21 13:58
 */

package com.lion.vip.api.router;

/**
 * 路由类型
 *
 * @param <T>
 */
public interface Router<T> {
    T getRouteValue();

    RouterType getRouterType();

    enum RouterType {
        LOCAL,    //本地路由
        REMOTE    //远程路由
    }
}
