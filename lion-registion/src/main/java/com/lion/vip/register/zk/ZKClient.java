package com.lion.vip.register.zk;

import com.lion.vip.api.Constants;
import com.lion.vip.api.service.BaseService;
import com.lion.vip.api.service.Listener;
import com.lion.vip.tools.log.Logs;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    private void initLocalCache(String watchPath) {

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

    public List<String> getChildrenKeys(String serviceName) {
    }

    public <R> R get(String s) {
    }

    public void registerListener(ZKCacheListener zkCacheListener) {
    }

    public void registerPersist(String nodePath, String toJson) {
    }

    public void registerEphemeral(String nodePath, String toJson) {
    }

    public boolean isRunning() {
    }

    public void remove(String nodePath) {

    }
}
