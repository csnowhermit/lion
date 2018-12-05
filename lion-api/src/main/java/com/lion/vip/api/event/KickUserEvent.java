package com.lion.vip.api.event;

/**
 * 踢人事件
 */
public final class KickUserEvent implements Event {
    private final String userId;        //用户ID
    private final String deviceId;      //设备ID
    private final String fromServer;    //来自哪台服务器上

    public KickUserEvent(String userId, String deviceId, String fromServer) {
        this.userId = userId;
        this.deviceId = deviceId;
        this.fromServer = fromServer;
    }
}
