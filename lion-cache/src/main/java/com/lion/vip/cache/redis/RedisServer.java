package com.lion.vip.cache.redis;

import com.lion.vip.tools.config.data.RedisNode;
import redis.clients.jedis.HostAndPort;

/**
 * Redis服务器
 */
public class RedisServer extends RedisNode {

    public RedisServer(String host, int port) {
        super(host, port);
    }

    public HostAndPort convert() {
        return new HostAndPort(host, port);
    }
}
