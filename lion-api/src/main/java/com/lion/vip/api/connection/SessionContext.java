package com.lion.vip.api.connection;

import com.lion.vip.api.router.ClientClassifier;

/**
 * Session上下文
 */
public final class SessionContext {

    public String osName;       //操作系统名称
    public String osVersion;    //操作系统版本号
    public String clientVersion;//客户端版本号
    public String deviceId;     //设备ID
    public String userId;       //用户ID
    public String tags;         //用户标识
    public int heartbeat = 1000;    //心跳检测周期，10s
    public Cipher cipher;       //数据加解密接口
    public byte clientType;    //客户端类型


    public SessionContext setOsName(String osName) {
        this.osName = osName;
        return this;
    }

    public SessionContext setOsVersion(String osVersion) {
        this.osVersion = osVersion;
        return this;
    }

    public SessionContext setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
        return this;
    }

    public SessionContext setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public SessionContext setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public void setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
    }

    public int getHeartbeat() {
        return heartbeat;
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
