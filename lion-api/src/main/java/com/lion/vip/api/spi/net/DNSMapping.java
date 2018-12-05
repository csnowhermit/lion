package com.lion.vip.api.spi.net;

import java.net.URL;
import java.util.Objects;

/**
 * DNS映射
 */
public class DNSMapping {
    protected String ip;
    protected int port;

    public DNSMapping() {
    }

    public DNSMapping(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public DNSMapping setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public int getPort() {
        return port;
    }

    public DNSMapping setPort(int port) {
        this.port = port;
        return this;
    }

    public static DNSMapping parse(String addr) {
        String[] host_port = Objects.requireNonNull(addr, "DNS Mapping can not be null").split(":");
        if (host_port.length == 1) {    //如果没写端口，默认80
            return new DNSMapping(host_port[0], 80);
        } else {
            return new DNSMapping(host_port[0], Integer.valueOf(host_port[1]));
        }
    }

    public String translate(URL url) {
        StringBuilder stringBuilder = new StringBuilder(128);
        stringBuilder.append(url.getPath()).append("://")
                .append(ip)
                .append(":")
                .append(port)
                .append(url.getPath());

        String query = url.getQuery();
        if (query != null) {
            stringBuilder.append("?").append(query);
        }
        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        DNSMapping that = (DNSMapping) obj;

        if (port != that.port) {
            return false;
        }
        return ip.equals(that.ip);
    }

    /**
     * 重写equals()方法后一定要重写hashCode()
     *
     * @return
     */
    @Override
    public int hashCode() {
        int result = ip.hashCode();
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return ip + ":" + port;
    }

}
