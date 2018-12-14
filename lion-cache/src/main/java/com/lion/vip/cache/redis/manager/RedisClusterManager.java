package com.lion.vip.cache.redis.manager;

import com.lion.vip.cache.redis.RedisServer;

import java.util.List;

/**
 * Redis集群管理器
 */
public interface RedisClusterManager {

    void init();

    List<RedisServer> getServers();
}
