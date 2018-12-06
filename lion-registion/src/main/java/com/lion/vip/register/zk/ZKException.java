package com.lion.vip.register.zk;

/**
 * zk 异常类
 */
public class ZKException extends RuntimeException {

    public ZKException() {
        super();
    }

    public ZKException(String message) {
        super(message);
    }

    public ZKException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZKException(Throwable cause) {
        super(cause);
    }

}
