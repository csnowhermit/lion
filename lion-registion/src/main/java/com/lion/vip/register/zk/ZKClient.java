package com.lion.vip.register.zk;

import com.lion.vip.api.Constants;
import com.lion.vip.api.service.BaseService;
import com.lion.vip.api.service.Listener;
import com.lion.vip.tools.log.Logs;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * zk客户端
 */
public class ZKClient extends BaseService {

    public static final ZKClient I = I();
    private ZKConfig zkConfig;
    private CuratorFramework client;
    private TreeCache cache;
    private Map<String, String> ephemeralNodes = new LinkedHashMap<>(4);
    private Map<String, String> ephemeralSequentialNodes = new LinkedHashMap<>(1);

    private synchronized static ZKClient I() {
        return I == null ? new ZKClient() : I;
    }

    private ZKClient() {
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

        client.start();
        Logs.RSD.info("init zk client waiting for connected...");
        if (!client.blockUntilConnected(1, TimeUnit.MINUTES)) {
            throw new ZKException("init zk error, config=" + zkConfig);
        }
        initLocalCache(zkConfig.getWatchPath());
        addConnectionStateListener();
        Logs.RSD.info("zk client start success, server lists is:{}", zkConfig.getHosts());
        listener.onSuccess(zkConfig.getHosts());

    }

    /**
     * 注册连接状态监听器
     */
    private void addConnectionStateListener() {
        client.getConnectionStateListenable().addListener((cli, newState) -> {
            if (newState == ConnectionState.RECONNECTED) {
                ephemeralNodes.forEach(this::reRegisterEphemeral);
                ephemeralSequentialNodes.forEach(this::reRegisterEphemeralSequential);
            }
            Logs.RSD.warn("zk connection state changed new state={}, isConnected={}", newState, newState.isConnected());
        });
    }

    private void reRegisterEphemeralSequential(final String key, final String value) {
        registerEphemeralSequential(key, value, false);
    }

    public void registerEphemeralSequential(final String key, final String value) {
        registerEphemeralSequential(key, value, true);
    }

    /**
     * 注册临时顺序数据
     *
     * @param key
     * @param value
     * @param cacheNode
     */
    private void registerEphemeralSequential(final String key, final String value, boolean cacheNode) {
        try {
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(key);
        } catch (Exception ex) {
            Logs.RSD.error("persistEphemeralSequential:{}", key, ex);
            throw new ZKException(ex);
        }
    }

    /**
     * 注册临时数据
     *
     * @param key
     * @param value
     */
    private void reRegisterEphemeral(final String key, final String value) {
        registerEphemeral(key, value, false);
    }

    /**
     * 注册临时数据
     *
     * @param nodePath
     * @param toJson
     */
    public void registerEphemeral(String nodePath, String toJson) {
        registerEphemeral(nodePath, toJson, true);
    }


    private void registerEphemeral(String key, String value, boolean b) {
        try {
            if (isExisted(key)) {
                client.delete().deletingChildrenIfNeeded().forPath(key);
            }
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(key, value.getBytes(Constants.UTF_8));
            if (b) {
                ephemeralNodes.put(key, value);
            }
        } catch (Exception ex) {
            Logs.RSD.error("persistEphemeral:{},{}", key, value, ex);
            throw new ZKException(ex);
        }
    }

    /**
     * 判断路径是否存在
     *
     * @param key
     * @return
     */
    public boolean isExisted(final String key) {
        try {
            return null != client.checkExists().forPath(key);
        } catch (Exception ex) {
            Logs.RSD.error("isExisted:{}", key, ex);
            return false;
        }
    }

    /**
     * 持久化数据
     *
     * @param key
     * @param value
     */
    public void registerPersist(final String key, final String value) {
        try {
            if (isExisted(key)) {
                update(key, value);
            } else {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(key, value.getBytes());
            }
        } catch (Exception ex) {
            Logs.RSD.error("persist:{},{}", key, value, ex);
            throw new ZKException(ex);
        }
    }

    /**
     * 更新数据
     *
     * @param key
     * @param value
     */
    public void update(final String key, final String value) {
        try {
//            TransactionOp op = client.transactionOp();
//            client.transaction().forOperations(
//                    op.check().forPath(key),
//                    op.setData().forPath(key, value.getBytes(Constants.UTF_8))
//            );
            client.inTransaction().check().forPath(key).and().setData().forPath(key, value.getBytes(Constants.UTF_8)).and().commit();
        } catch (Exception ex) {
            Logs.RSD.error("update:{},{}", key, value, ex);
            throw new ZKException(ex);
        }
    }


    /**
     * 启用本地缓存
     *
     * @param watchPath
     */
    private void initLocalCache(String watchPath) throws Exception {
        cache = new TreeCache(client, watchPath);
        cache.start();
    }

    @Override
    protected void doStop(Listener listener) throws Throwable {
        if (cache != null) {
            cache.close();
        }

        TimeUnit.MILLISECONDS.sleep(600);
        client.close();
        Logs.RSD.info("zk client closed...");
        listener.onSuccess();
    }

    /**
     * 初始化
     */
    @Override
    public void init() {
        if (client != null) {
            return;
        }
        if (zkConfig == null) {
            zkConfig = ZKConfig.build();
        }

        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(zkConfig.getHosts())
                .retryPolicy(new ExponentialBackoffRetry(zkConfig.getBaseSleepTimeMs(), zkConfig.getMaxRetries(), zkConfig.getMaxSleepMs()))
                .namespace(zkConfig.getNamespace());

        if (zkConfig.getConnectionTimeout() > 0) {
            builder.connectionTimeoutMs(zkConfig.getConnectionTimeout());
        }

        if (zkConfig.getSessionTimeout() > 0) {
            builder.sessionTimeoutMs(zkConfig.getSessionTimeout());
        }

        if (zkConfig.getDigest() != null) {
            /*
             * scheme对应于采用哪种方案来进行权限管理，zookeeper实现了一个pluggable的ACL方案，可以通过扩展scheme，来扩展ACL的机制。
             * zookeeper缺省支持下面几种scheme:
             *
             * world: 默认方式，相当于全世界都能访问; 它下面只有一个id, 叫anyone, world:anyone代表任何人，zookeeper中对所有人有权限的结点就是属于world:anyone的
             * auth: 代表已经认证通过的用户(cli中可以通过addauth digest user:pwd 来添加当前上下文中的授权用户); 它不需要id, 只要是通过authentication的user都有权限（zookeeper支持通过kerberos来进行authencation, 也支持username/password形式的authentication)
             * digest: 即用户名:密码这种方式认证，这也是业务系统中最常用的;它对应的id为username:BASE64(SHA1(password))，它需要先通过username:password形式的authentication
             * ip: 使用Ip地址认证;它对应的id为客户机的IP地址，设置的时候可以设置一个ip段，比如ip:192.168.1.0/16, 表示匹配前16个bit的IP段
             * super: 在这种scheme情况下，对应的id拥有超级权限，可以做任何事情(cdrwa)
             */

            builder.authorization("digest", zkConfig.getDigest().getBytes(Constants.UTF_8));
            builder.aclProvider(new ACLProvider() {
                @Override
                public List<ACL> getDefaultAcl() {
                    return ZooDefs.Ids.CREATOR_ALL_ACL;
                }

                @Override
                public List<ACL> getAclForPath(String s) {
                    return ZooDefs.Ids.CREATOR_ALL_ACL;
                }
            });

            client = builder.build();
            Logs.RSD.info("init zk client, config={}", zkConfig.toString());
        }

    }

    /**
     * 获取子节点
     *
     * @param serviceName
     * @return
     */
    public List<String> getChildrenKeys(String serviceName) {
        try {
            if (!isExisted(serviceName)) {
                return Collections.emptyList();
            }
            List<String> result = client.getChildren().forPath(serviceName);
            result.sort(Comparator.reverseOrder());
            return result;
        } catch (Exception ex) {
            Logs.RSD.error("getChildrenKeys:{}", serviceName, ex);
            return Collections.emptyList();
        }
    }

    /**
     * 获取数据：先从本地获取，若没有，则从远端获取
     *
     * @param key
     * @return
     */
    public String get(String key) {
        if (null == cache) {
            return null;
        }
        ChildData data = cache.getCurrentData(key);
        if (null != data) {
            return null == data.getData() ? null : new String(data.getData(), Constants.UTF_8);
        }
        return getFromRemote(key);

    }

    /**
     * 从远程获取数据
     *
     * @param nodePath
     * @return
     */
    public String getFromRemote(final String nodePath) {
        if (isExisted(nodePath)) {
            try {
                return new String(client.getData().forPath(nodePath), Constants.UTF_8);
            } catch (Exception ex) {
                Logs.RSD.error("getFromRemote:{}", nodePath, ex);
            }
        }
        return null;
    }


    /**
     * 注册监听器
     *
     * @param zkCacheListener
     */
    public void registerListener(ZKCacheListener zkCacheListener) {
        cache.getListenable().addListener(zkCacheListener);
    }

    public boolean isRunning() {
        return super.isRunning();
    }

    /**
     * 删除数据
     *
     * @param nodePath
     */
    public void remove(String nodePath) {
        try {
            client.delete().deletingChildrenIfNeeded().forPath(nodePath);
        } catch (Exception ex) {
            Logs.RSD.error("removeAndClose:{}", nodePath, ex);
            throw new ZKException(ex);
        }
    }

    public ZKConfig getZKConfig() {
        return zkConfig;
    }

    public ZKClient setZKConfig(ZKConfig zkConfig) {
        this.zkConfig = zkConfig;
        return this;
    }

    public CuratorFramework getClient() {
        return client;
    }
}
