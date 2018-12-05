/**
 * FileName: ClientLocation
 * Author:   Ren Xiaotian
 * Date:     2018/11/21 15:49
 */

package com.lion.vip.api.router;

import com.lion.vip.api.connection.Connection;
import com.lion.vip.api.connection.SessionContext;

/**
 * 客户端所在的主机信息
 */
public final class ClientLocation {
    private String host;             //长连接所在的主机
    private int port;                //长连接所在的端口
    private String osName;           //客户端的操作系统
    private String clientVersion;    //客户端版本号
    private String deviceId;         //设备ID
    private String connId;           //连接ID
    private transient int clientType;    //客户端类型

    public String getHost() {
        return host;
    }

    public ClientLocation setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public ClientLocation setPort(int port) {
        this.port = port;
        return this;
    }

    public String getOsName() {
        return osName;
    }

    public ClientLocation setOsName(String osName) {
        this.osName = osName;
        return this;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public ClientLocation setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
        return this;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public ClientLocation setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public String getConnId() {
        return connId;
    }

    public ClientLocation setConnId(String connId) {
        this.connId = connId;
        return this;
    }

    public int getClientType() {
        return clientType;
    }

    public void setClientType(int clientType) {
        this.clientType = clientType;
    }

    /**
     * 是否上线
     *
     * @return
     */
    public boolean isOnline() {
        return connId != null;
    }

    /**
     * 是否下线
     *
     * @return
     */
    public boolean isOffline() {
        return connId == null;
    }

    /**
     * 下线操作
     *
     * @return
     */
    public ClientLocation offline() {
        this.connId = null;
        return this;
    }

    /**
     * 判断某个ip和端口是否是当前的主机
     * @param host
     * @param port
     * @return
     */
    public boolean isThisMachine(String host, int port){
        return this.port == port && this.host.equals(host);
    }

    public String getHostAndPort(){
        return this.host + ":" + this.port;
    }

    public static ClientLocation from(Connection connection){
        SessionContext sessionContext = connection.getSessionContext();
        ClientLocation clientLocation = new ClientLocation();
        clientLocation.setOsName(sessionContext.getOsName());
        clientLocation.setClientVersion(sessionContext.getClientVersion());
        clientLocation.setDeviceId(sessionContext.getDeviceId());
        clientLocation.setConnId(connection.getId());

        return clientLocation;
    }

    public String toJson() {
        return "{"
                + "\"port\":" + port
                + (host == null ? "" : ",\"host\":\"" + host + "\"")
                + (deviceId == null ? "" : ",\"deviceId\":\"" + deviceId + "\"")
                + (osName == null ? "" : ",\"osName\":\"" + osName + "\"")
                + (clientVersion == null ? "" : ",\"clientVersion\":\"" + clientVersion + "\"")
                + (connId == null ? "" : ",\"connId\":\"" + connId + "\"")
                + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientLocation location = (ClientLocation) o;

        return clientType == location.clientType;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(clientType);
    }

    @Override
    public String toString() {
        return "ClientLocation{" +
                "host='" + host + ":" + port + "\'" +
                ", osName='" + osName + '\'' +
                ", clientVersion='" + clientVersion + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", connId='" + connId + '\'' +
                '}';
    }

}
