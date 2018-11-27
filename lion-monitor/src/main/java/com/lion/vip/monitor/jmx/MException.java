/**
 * FileName: MException
 * Author:   Ren Xiaotian
 * Date:     2018/11/26 14:30
 */

package com.lion.vip.monitor.jmx;

public class MException extends RuntimeException {
    public MException(String message) {
        super(message);
    }

    public MException(String message, Throwable cause) {
        super(message, cause);
    }

    public MException(Throwable cause) {
        super(cause);
    }
}
