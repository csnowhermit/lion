package com.lion.vip.cache.redis.connection;

import com.lion.vip.cache.redis.RedisException;
import com.lion.vip.tools.config.data.RedisNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import redis.clients.util.Pool;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Redis连接工厂类
 */
public class RedisConnectionFactory {
    private final static Logger LOGGER = LoggerFactory.getLogger(RedisConnectionFactory.class);

    private String hostName = "localhost";    //默认连接本地redis
    private int port = Protocol.DEFAULT_PORT;
    private int timeout = Protocol.DEFAULT_TIMEOUT;
    private String password;

    //哨兵模式下
    private String sentineMaster;    //哨兵的主节点
    private List<RedisNode> redisNodeList;    //哨兵模式下所有节点
    private boolean isCluster = false;    //是否为集群模式，默认否
    private int dbIndex = 0;

    //集群模式下
    private JedisShardInfo shardInfo;    //分片信息
    private Pool<Jedis> pool;    //连接池
    private JedisCluster jedisCluster;    //集群
    private JedisPoolConfig poolConfig = new JedisPoolConfig();

    public RedisConnectionFactory() {
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSentineMaster() {
        return sentineMaster;
    }

    public void setSentineMaster(String sentineMaster) {
        this.sentineMaster = sentineMaster;
    }

    public List<RedisNode> getRedisNodeList() {
        return redisNodeList;
    }

    public void setRedisNodeList(List<RedisNode> redisNodeList) {
        this.redisNodeList = redisNodeList;
    }

    public boolean isCluster() {
        return isCluster;
    }

    public void setCluster(boolean cluster) {
        isCluster = cluster;
    }

    public int getDbIndex() {
        return dbIndex;
    }

    public void setDbIndex(int dbIndex) {
        this.dbIndex = dbIndex;
    }

    public JedisShardInfo getShardInfo() {
        return shardInfo;
    }

    public void setShardInfo(JedisShardInfo shardInfo) {
        this.shardInfo = shardInfo;
    }

    public Pool<Jedis> getPool() {
        return pool;
    }

    public void setPool(Pool<Jedis> pool) {
        this.pool = pool;
    }

    public JedisCluster getJedisCluster() {
        return jedisCluster;
    }

    public JedisCluster getClusterConnection() {
        return jedisCluster;
    }

    public void setJedisCluster(JedisCluster jedisCluster) {
        this.jedisCluster = jedisCluster;
    }

    public JedisPoolConfig getPoolConfig() {
        return poolConfig;
    }

    public void setPoolConfig(JedisPoolConfig poolConfig) {
        this.poolConfig = poolConfig;
    }

    /**
     * 获取Jedis连接
     *
     * @return
     */
    public Jedis fetchJedisConnector() {
        try {
            if (pool != null) {
                return pool.getResource();
            }
            Jedis jedis = new Jedis(getShardInfo());
            jedis.connect();
            return jedis;
        } catch (RedisException ex) {
            throw new RedisException("Cannot get Jedis connection ", ex);
        }
    }

    public void init() {
        if (shardInfo == null) {
            shardInfo = new JedisShardInfo(hostName, port);

            if (StringUtils.isNotEmpty(password)) {
                shardInfo.setPassword(password);    //设置密码
            }

            if (timeout > 0) {
                shardInfo.setSoTimeout(timeout);    //设置超时时间
            }
        }

        if (isCluster) {    //如果是集群模式，则创建集群
            this.jedisCluster = createCluster();
        } else {    //否则创建连接池
            this.pool = createPool();
        }
    }

    /**
     * 创建Redis连接池
     *
     * @return
     */
    private Pool<Jedis> createPool() {
        //如果是哨兵模式，则创建Redis哨兵连接池
        if (StringUtils.isNotBlank(sentineMaster)) {
            return createRedisSentinelPool();
        }

        //否则创建普通连接池
        return createRedisPool();
    }

    /**
     * 创建普通Jedis连接池
     *
     * @return
     */
    private Pool<Jedis> createRedisPool() {
        return new JedisPool(getPoolConfig(), shardInfo.getHost(), shardInfo.getPort(), shardInfo.getSoTimeout(), shardInfo.getPassword());
    }

    /**
     * 创建Redis是哨兵连接池
     *
     * @return
     */
    private Pool<Jedis> createRedisSentinelPool() {
        Set<String> hostAndPosts = redisNodeList.stream()
                .map(redisNode -> new HostAndPort(redisNode.host, redisNode.port).toString())
                .collect(Collectors.toSet());

        return new JedisSentinelPool(sentineMaster, hostAndPosts, poolConfig, getShardInfo().getSoTimeout(), getShardInfo().getPassword());
    }

    /**
     * 创建cluster集群
     *
     * @return
     */
    private JedisCluster createCluster() {
        Set<HostAndPort> hostAndPorts = redisNodeList.stream()
                .map(redisNode -> new HostAndPort(redisNode.host, redisNode.port))
                .collect(Collectors.toSet());

        if (StringUtils.isNotEmpty(getPassword())) {
            throw new IllegalArgumentException("Jedis does not support password protected Redis Cluster configurations!");
        }

        int redirects = 5;
        return new JedisCluster(hostAndPorts, timeout, redirects, poolConfig);
    }

    /**
     * 关闭连接池及集群
     */
    public void destroy() {
        try {
            if (pool != null) {
                pool.destroy();
            }
        } catch (Exception e) {
            LOGGER.warn("Cannot properly close Jedis Pool", e);
        } finally {
            pool = null;
        }


        try {
            if (jedisCluster != null) {
                jedisCluster.close();
            }
        } catch (Exception e) {
            LOGGER.warn("Cannot properly close Jedis CLuster", e);
        } finally {
            jedisCluster = null;
        }
    }

    public Jedis getJedisConnection() {
        Jedis jedis = fetchJedisConnector();
        if (dbIndex > 0 && jedis != null) {
            jedis.select(dbIndex);
        }

        return jedis;
    }

}
