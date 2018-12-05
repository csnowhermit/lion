/**
 * FileName: SessionContext
 * Author:   Ren Xiaotian
 * Date:     2018/11/21 11:33
 */

package com.lion.vip.api.connection;

import com.lion.vip.api.router.ClientClassifier;

/**
 * Session上下文
 */
public final class SessionContext {

    private String osName;       //操作系统名称
    private String osVersion;    //操作系统版本号
    private String clientVersion;//客户端版本号
    private String deviceId;     //设备ID
    public String userId;       //用户ID
    private String tags;         //用户标识
    private int heartbeat = 1000;    //心跳检测周期，10s
    public Cipher cipher;       //数据加解密接口
    private byte clientType;    //客户端类型

    public String getOsName() {
        return osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getUserId() {
        return userId;
    }

    public String getTags() {
        return tags;
    }

    public int getHeartbeat() {
        return heartbeat;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public void setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
    }

    public void changeCipher(Cipher cipher) {
        this.cipher = cipher;
    }

    public void setClientType(byte clientType) {
        this.clientType = clientType;
    }

    public boolean handshakeOk(){
        return deviceId != null && deviceId.length()>0;
    }

    public byte getClientType() {
        if (clientType == 0){
            clientType = (byte) ClientClassifier.I.getClientType(osName);
        }
        return clientType;
    }

    public boolean isSecurity(){
        return cipher != null;
    }

    @Override
    public String toString() {
        return "SessionContext{" +
                "osName='" + osName + '\'' +
                ", osVersion='" + osVersion + '\'' +
                ", clientVersion='" + clientVersion + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", userId='" + userId + '\'' +
                ", tags='" + tags + '\'' +
                ", heartbeat=" + heartbeat +
                ", cipher=" + cipher +
                ", clientType=" + clientType +
                '}';
    }
}
