package com.lion.vip.cache.redis.manager;

import com.lion.vip.api.spi.common.CacheManager;
import com.lion.vip.cache.redis.connection.RedisConnectionFactory;
import com.lion.vip.tools.Jsons;
import com.lion.vip.tools.config.CC;
import com.lion.vip.tools.log.Logs;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.JedisPoolConfig;

import java.io.StringWriter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Redis管理器
 */
public final class RedisManager implements CacheManager {
    private static final RedisManager I = new RedisManager();
    private final RedisConnectionFactory factory = new RedisConnectionFactory();

    @Override
    public void init() {
        Logs.CACHE.info("begin init redis...");

        factory.setPassword(CC.lion.redis.password);
        factory.setPoolConfig(CC.lion.redis.getPoolConfig(JedisPoolConfig.class));
        factory.setRedisNodeList(CC.lion.redis.nodes);
        factory.setCluster(CC.lion.redis.isCluster());    //集群模式

        if (CC.lion.redis.isSentinel()) {    //是否哨兵模式
            factory.setSentineMaster(CC.lion.redis.sentinelMaster);
        }

        factory.init();
        test();    //测试，能到拿到redis集群或连接
        Logs.CACHE.info("init redis success...");
    }

    /**
     * 测试：能否拿到redis集群或连接
     */
    private void test() {
        if (factory.isCluster()) {
            JedisCluster cluster = factory.getClusterConnection();

            if (cluster == null) {
                throw new RuntimeException("init redis cluster error");
            }
        } else {
            Jedis jedis = factory.getJedisConnection();

            if (jedis == null) {
                throw new RuntimeException("init redis error, can not get Connection");
            }
            jedis.close();
        }
    }

    @Override
    public void destroy() {
        if (factory != null) {
            factory.destroy();
        }
    }


    private <R> R call(Function<JedisCommands, R> function, R d) {
        if (factory.isCluster()) {    //集群模式下
            try {
                return function.apply(factory.getJedisCluster());
            } catch (Exception ex) {
                Logs.CACHE.error("redix ex", ex);
                throw new RuntimeException(ex);
            }
        } else {    //非集群模式下
            try (Jedis jedis = factory.getJedisConnection()) {
                return function.apply(jedis);
            } catch (Exception ex) {
                Logs.CACHE.error("redis ex", ex);
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * 函数式调用式：通过Jedis对redis进行调用
     *
     * @param consumer
     */
    private void call(Consumer<JedisCommands> consumer) {
        if (factory.isCluster()) {
            try {
                consumer.accept(factory.getClusterConnection());
            } catch (Exception ex) {
                Logs.CACHE.error("redix ex", ex);
                throw new RuntimeException(ex);
            }
        } else {
            try (Jedis jedis = factory.getJedisConnection()) {
                consumer.accept(jedis);
            } catch (Exception ex) {
                Logs.CACHE.error("redix ex", ex);
                throw new RuntimeException(ex);
            }
        }
    }

    /////////////////////////////对Redis的操作/////////////////////////////

    @Override
    public void del(String key) {
        call(jedisCommands -> jedisCommands.del(key));
    }

    @Override
    public long hincrBy(String key, String field, long value) {
        return call(jedisCommands -> jedisCommands.hincrBy(key, field, value), 0L);
    }

    @Override
    public void set(String key, String value) {
        call(jedisCommands -> jedisCommands.set(key, value)
        );
    }

    @Override
    public void set(String key, String value, int expireTime) {
        call(jedisCommands -> {
            jedisCommands.set(key, value);
            if (expireTime > 0) {
                jedisCommands.expire(key, expireTime);
            }
        });
    }

    @Override
    public void set(String key, Object value, int expireTime) {
        set(key, Jsons.toJson(value), expireTime);
    }

    @Override
    public <T> T get(String key, Class<T> tClass) {
        return null;
    }

    @Override
    public void hset(String key, String field, String value) {
        call(jedisCommands -> jedisCommands.hset(key, field, value));
    }

    @Override
    public void hset(String key, String field, Object value) {
        hset(key, field, Jsons.toJson(value));
    }

    @Override
    public <T> T hget(String key, String field, Class<T> tClass) {
        String value = call(jedisCommands -> jedisCommands.hget(key, field), null);

        if (value == null) {
            return null;
        }
        if (tClass == String.class) {
            return (T) value;
        }

        return Jsons.fromJson(value, tClass);
    }

    @Override
    public void hdel(String key, String field) {
        call(jedisCommands -> jedisCommands.hdel(key, field));
    }

    @Override
    public <T> Map<String, T> hgetAll(String key, Class<T> clazz) {
        Map<String, String> result = hgetAll(key);
        if (result.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, T> newMap = new HashMap<>(result.size());
        result.forEach((k, v) -> newMap.put(k, Jsons.fromJson(v, clazz)));

        return newMap;
    }

    private Map<String, String> hgetAll(String key) {
        return call(jedisCommands -> jedisCommands.hgetAll(key),
                Collections.<String, String>emptyMap());
    }

    public Set<String> hkeys(String key) {
        return call(jedisCommands -> jedisCommands.hkeys(key),
                Collections.<String>emptySet());
    }

    /**
     * 返回key 指定的哈希集中指定字段的值
     *
     * @param key
     * @param clazz
     * @param fields
     * @param <T>
     * @return
     */
    public <T> List<T> hmget(String key, Class<T> clazz, String... fields) {
        return call(jedisCommands -> jedisCommands.hmget(key, fields),
                Collections.<String>emptyList())
                .stream()
                .map(s -> Jsons.fromJson(s, clazz))
                .collect(Collectors.toList());
    }

    public void hmset(String key, Map<String, String> hash, int time) {
        call(jedisCommands -> {
            jedisCommands.hmset(key, hash);
            if (time>0){
                jedisCommands.expire(key, time);
            }
        });
    }

    public void hmset(String key, Map<String, String> hash) {
        call(jedisCommands -> jedisCommands.hmset(key, hash));
    }

    @Override
    public void zAdd(String key, String value) {
        call(jedisCommands -> jedisCommands.zadd(key, 0, value));
    }

    @Override
    public Long zCard(String key) {
        return null;
    }

    @Override
    public void zRem(String key, String value) {

    }

    @Override
    public <T> List<T> zrange(String key, int start, int end, Class<T> clazz) {
        return null;
    }

    @Override
    public void lpush(String key, String... value) {

    }

    @Override
    public <T> List<T> lrange(String key, int start, int end, Class<T> clazz) {
        return null;
    }
}
