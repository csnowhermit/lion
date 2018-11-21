/**
 * FileName: Listener
 * Author:   Ren Xiaotian
 * Date:     2018/11/21 10:06
 */

package com.lion.vip.api.service;

/**
 * 监听器
 */
public interface Listener {

    void onSuccess(Object... args);

    void onFailure(Throwable cause);
}
