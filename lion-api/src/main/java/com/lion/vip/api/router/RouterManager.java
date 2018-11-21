/**
 * FileName: RouterManager
 * Author:   Ren Xiaotian
 * Date:     2018/11/21 15:35
 */

package com.lion.vip.api.router;

import java.util.Set;

/**
 * 路由管理器
 *
 * @param <R>
 */
public interface RouterManager<R extends Router> {

    /**
     * 注册路由
     *
     * @param userId 用户ID
     * @param router 新路由
     * @return 返回新路由；如果以前注册过返回旧路由
     */
    R register(String userId, R router);

    /**
     * 删除路由
     *
     * @param userId     用户ID
     * @param clientType 客户端类型
     * @return true，成功；false，失败
     */
    boolean unregister(String userId, String clientType);

    /**
     * 根据用户ID查找路由
     *
     * @param userId 用户ID
     * @return 返回该用户所有的路由集合
     */
    Set<R> lookupAll(String userId);

    /**
     * 根据用户ID和客户端类型查询路由信息
     *
     * @param userId     用户ID
     * @param clientType 客户端类型
     * @return 指定类型的路由信息
     */
    R lookup(String userId, String clientType);

}
