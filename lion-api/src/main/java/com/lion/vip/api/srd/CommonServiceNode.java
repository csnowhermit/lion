/**
 * FileName: CommonServiceNode
 * Author:   Ren Xiaotian
 * Date:     2018/11/21 16:55
 */

package com.lion.vip.api.srd;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 同一服务，所有节点的集合
 */
public class CommonServiceNode implements ServiceNode {
    private String host;
    private int port;
    private Map<String, Object> attrs;       //同一服务，所有节点的集合
    private transient String serviceName;    //服务名称
    private transient String nodeId;         //节点ID
    private transient boolean persistent;

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    public CommonServiceNode addAttr(String name, Object value) {
        if (attrs == null) {
            attrs = new HashMap<>();
        }
        attrs.put(name, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAttr(String name) {
        if (attrs == null || attrs.isEmpty()) {
            return null;
        } else {
            return (T) attrs.get(name);
        }
    }

    @Override
    public boolean isPersistent() {
        return persistent;
    }

    @Override
    public String hostAndPort() {
        return this.host + ":" + this.port;
    }

    @Override
    public String serviceName() {
        return serviceName;
    }

    @Override
    public String nodeId() {
        if (nodeId == null || "".equals(nodeId)) {
            nodeId = UUID.randomUUID().toString();
        }
        return nodeId;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    public Map<String, Object> getAttrs() {
        return attrs;
    }

    public void setAttrs(Map<String, Object> attrs) {
        this.attrs = attrs;
    }

    @Override
    public String toString() {
        return "CommonServiceNode{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", attrs=" + attrs +
                ", serviceName='" + serviceName + '\'' +
                ", nodeId='" + nodeId + '\'' +
                ", persistent=" + persistent +
                '}';
    }
}
