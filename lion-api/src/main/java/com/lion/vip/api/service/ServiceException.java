/**
 * FileName: ServiceException
 * Author:   Ren Xiaotian
 * Date:     2018/11/21 10:08
 */

package com.lion.vip.api.service;

/**
 * 自定义服务异常类
 */
public class ServiceException extends RuntimeException {
    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(Throwable cause) {
        super(cause);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
