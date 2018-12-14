package com.lion.vip.cache.redis;

/**
 * Redis自定义异常
 */
public class RedisException extends RuntimeException {

    public RedisException() {
    }

    public RedisException(String message) {
        super(message);
    }

    public RedisException(Throwable cause) {
        super(cause);
    }

    public RedisException(String message, Throwable cause) {
        super(message, cause);
    }
}
