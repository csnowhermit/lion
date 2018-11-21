/**
 * FileName: ServiceNode
 * Author:   Ren Xiaotian
 * Date:     2018/11/21 16:46
 */

package com.lion.vip.api.srd;

/**
 * 服务节点
 */
public interface ServiceNode {

    String serviceName();

    String nodeId();

    String getHost();

    int getPort();

    default <T> T getAttr(String name) {
        return null;
    }

    default boolean isPersistent() {
        return false;
    }

    default String hostAndPort() {
        return getHost() + ":" + getPort();
    }

    default String nodePath() {
        return serviceName() + "/" + nodeId();
    }

}
