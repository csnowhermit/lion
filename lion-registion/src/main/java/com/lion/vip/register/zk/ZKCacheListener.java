package com.lion.vip.register.zk;

import com.google.common.base.Strings;
import com.lion.vip.api.srd.CommonServiceNode;
import com.lion.vip.api.srd.ServiceListener;
import com.lion.vip.tools.Jsons;
import com.lion.vip.tools.log.Logs;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;

/**
 * zk 缓存监听器
 */
public class ZKCacheListener implements TreeCacheListener {

    private final String watchPath;    //要监听的目录
    private final ServiceListener serviceListener;    //监听器

    public ZKCacheListener(String watchPath, ServiceListener serviceListener) {
        this.watchPath = watchPath;
        this.serviceListener = serviceListener;
    }

    @Override
    public void childEvent(CuratorFramework curatorFramework, TreeCacheEvent treeCacheEvent) throws Exception {
        ChildData data = treeCacheEvent.getData();

        if (data == null) {
            return;
        }

        String dataPath = data.getPath();
        if (Strings.isNullOrEmpty(dataPath)) {
            return;
        }

        if (dataPath.startsWith(watchPath)) {
            switch (treeCacheEvent.getType()) {
                case NODE_ADDED:
                    serviceListener.onServiceAdded(dataPath, Jsons.fromJson(data.getData(), CommonServiceNode.class));
                    break;
                case NODE_REMOVED:
                    serviceListener.onServiceRemoved(dataPath, Jsons.fromJson(data.getData(), CommonServiceNode.class));
                    break;
                case NODE_UPDATED:
                    serviceListener.onServiceUpdated(dataPath, Jsons.fromJson(data.getData(), CommonServiceNode.class));
                    break;
            }
            Logs.RSD.info("ZK node data change={}, nodePath={}, watchPath={}, ns={}");
        }
    }
}
