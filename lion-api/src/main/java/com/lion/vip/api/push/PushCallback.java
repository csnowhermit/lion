package com.lion.vip.api.push;

import com.lion.vip.api.router.ClientLocation;

/**
 * 推送消息并回调
 */
public interface PushCallback {
    default void onResult(PushResult result) {
        switch (result.resultCode) {
            case PushResult.CODE_SUCCESS:
                onSuccess(result.userId, result.location);
                break;
            case PushResult.CODE_FAILURE:
                onFailure(result.userId, result.location);
                break;
            case PushResult.CODE_OFFLINE:
                onOffline(result.userId, result.location);
                break;
            case PushResult.CODE_TIMEOUT:
                onTimeout(result.userId, result.location);
                break;
        }
    }

    /**
     * 推送成功，指定用户推送时重写此方法
     *
     * @param userId   成功的用户，如果是广播，值为空
     * @param location 用户所在机器，如果是广播，值为空
     */
    default void onSuccess(String userId, ClientLocation location) {
    }

    /**
     * 推送失败，指定用户推送时重写此方法
     *
     * @param userId   失败的用户，如果是广播，值为空
     * @param location 用户所在机器，如果是广播，值为空
     */
    default void onFailure(String userId, ClientLocation location) {
    }

    /**
     * 推送的用户下线
     *
     * @param userId   下线的用户
     * @param location 用户所在机器
     */
    default void onOffline(String userId, ClientLocation location) {
    }

    /**
     * 推送超时
     *
     * @param userId   推送用户
     * @param location 用户所在机器
     */
    default void onTimeout(String userId, ClientLocation location) {
    }
}
