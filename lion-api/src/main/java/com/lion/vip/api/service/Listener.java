package com.lion.vip.api.service;

/**
 * 监听器
 */
public interface Listener {

    void onSuccess(Object... args);

    void onFailure(Throwable cause);
}
