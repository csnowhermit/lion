/**
 * FileName: PushException
 * Author:   Ren Xiaotian
 * Date:     2018/11/21 15:01
 */

package com.lion.vip.api.push;

/**
 * 推送异常类
 */
public class PushException extends RuntimeException {
    public PushException(String message) {
        super(message);
    }

    public PushException(Throwable cause) {
        super(cause);
    }

    public PushException(String message, Throwable cause) {
        super(message, cause);
    }
}
