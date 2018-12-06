package com.lion.vip.register.zk;


import com.lion.vip.api.service.BaseService;
import com.lion.vip.api.service.Listener;
import com.lion.vip.api.srd.*;
import com.lion.vip.tools.Jsons;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.apache.curator.utils.ZKPaths.PATH_SEPARATOR;

/**
 * zk 服务注册与发现
 */
public class ZKServiceRegistryAndDiscovery extends BaseService implements ServiceRegistry, ServiceDiscovery {

    public static final ZKServiceRegistryAndDiscovery I = new ZKServiceRegistryAndDiscovery();

    private final ZKClient client;

    public ZKServiceRegistryAndDiscovery() {
        this.client = ZKClient.I;
    }

    @Override
    public void start(Listener listener) {
        if (isRunning()) {
            listener.onSuccess();
        } else {
            super.start(listener);
        }
    }

    @Override
    public void stop(Listener listener) {
        if (isRunning()) {
            super.stop(listener);
        } else {
            listener.onSuccess();
        }
    }

    @Override
    protected void doStart(Listener listener) throws Throwable {
        client.start(listener);
    }

    @Override
    protected void doStop(Listener listener) throws Throwable {
        client.stop(listener);
    }

    @Override
    public List<ServiceNode> lookup(String serviceName) {
        List<String> childrenKeys = client.getChildrenKeys(serviceName);
        if (childrenKeys == null || childrenKeys.isEmpty()) {
            return Collections.emptyList();
        }

        return childrenKeys.stream()
                .map(key -> serviceName + PATH_SEPARATOR + key)
                .map(client::get)
                .filter(Objects::nonNull)
                .map(childData -> Jsons.fromJson(childData.toString(), CommonServiceNode.class))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void subscribe(String path, ServiceListener serviceListener) {
        client.registerListener(new ZKCacheListener(path, serviceListener));
    }

    @Override
    public void unsubscribe(String path, ServiceListener serviceListener) {

    }

    @Override
    public void register(ServiceNode serviceNode) {
        if (serviceNode.isPersistent()) {
            //创建持久节点
            client.registerPersist(serviceNode.nodePath(), Jsons.toJson(serviceNode));
        } else {
            //创建临时节点
            client.registerEphemeral(serviceNode.nodePath(), Jsons.toJson(serviceNode));
        }
    }

    @Override
    public void deregister(ServiceNode serviceNode) {
        if (client.isRunning()) {
            client.remove(serviceNode.nodePath());
        }
    }
}
