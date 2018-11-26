/**
 * FileName: HttpCallback
 * Author:   Ren Xiaotian
 * Date:     2018/11/23 16:02
 */

package com.lion.vip.network.netty.http;

import io.netty.handler.codec.http.HttpResponse;

/**
 * http 回调接口
 */
public interface HttpCallback {
    void onResponse(HttpResponse response);

    void onFailure(int statusCode, String reasonPhrase);

    void onException(Throwable throwable);

    void onTimeout();

    boolean onRedirect(HttpResponse response);
}
