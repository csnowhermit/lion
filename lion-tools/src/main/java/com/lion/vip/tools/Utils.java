package com.lion.vip.tools;

import com.lion.vip.tools.config.CC;
import com.lion.vip.tools.thread.NamedThreadFactory;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SingleThreadEventLoop;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.ThreadProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Pattern;

public class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    private static String LOCAL_IP;

    private static final Pattern LOCAL_IP_PATTERN = Pattern.compile("127(\\.\\d{1,3}){3}$");

    private static String EXTRANET_IP;

    private static final NamedThreadFactory NAMED_THREAD_FACTORY = new NamedThreadFactory();

    public static Thread newThread(String name, Runnable target) {
        return NAMED_THREAD_FACTORY.newThread(name, target);
    }

    public static boolean useNettyEpoll() {
        if (CC.lion.core.useNettyEpoll()) {
            try {
                Class.forName("io.netty.channel.epoll.Native");
                return true;
            } catch (Throwable error) {
                LOGGER.warn("can not load netty epoll, switch nio model.");
            }
        }
        return false;
    }

    /**
     * 判断是否是本机IP
     *
     * @param host
     * @return
     */
    public static boolean isLocalHost(String host) {
        return host == null
                || host.length() == 0
                || "0.0.0.0".equals(host)
                || ((LOCAL_IP_PATTERN.matcher(host)).matches());
    }

    public static String lookupLocalIP() {
        if (LOCAL_IP == null) {
            LOCAL_IP = getInetAddress(true);
        }
        return LOCAL_IP;
    }

    /**
     * 获得本地网卡
     *
     * @return
     */
    public static NetworkInterface getLocalNetworkInterface() {
        Enumeration<NetworkInterface> interfaceEnumeration;
        try {
            interfaceEnumeration = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new RuntimeException("NetworkInterface not found: {}", e);
        }
        while (interfaceEnumeration.hasMoreElements()) {
            NetworkInterface networkInterface = interfaceEnumeration.nextElement();
            Enumeration<InetAddress> addressEnumeration = networkInterface.getInetAddresses();
            while (addressEnumeration.hasMoreElements()) {
                InetAddress address = addressEnumeration.nextElement();
                if (address.isLoopbackAddress()) {
                    continue;
                }
                if (address.getHostAddress().contains(":")) {
                    continue;
                }
                if (address.isSiteLocalAddress()) {
                    return networkInterface;
                }
            }
        }
        throw new RuntimeException("NetworkInterface not found");
    }

    public static InetAddress getInetAddress(String host) {
        try {
            return InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("UnknownHost " + host, e);
        }
    }

    /**
     * 只获取第一块网卡绑定的ip地址
     *
     * @param getLocal 局域网IP
     * @return ip
     */
    public static String getInetAddress(boolean getLocal) {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                Enumeration<InetAddress> addresses = interfaces.nextElement().getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address.isLoopbackAddress()) continue;
                    if (address.getHostAddress().contains(":")) continue;
                    if (getLocal) {
                        if (address.isSiteLocalAddress()) {
                            return address.getHostAddress();
                        }
                    } else {
                        if (!address.isSiteLocalAddress()) {
                            return address.getHostAddress();
                        }
                    }
                }
            }
            LOGGER.debug("getInetAddress is null, getLocal={}", getLocal);
            return getLocal ? "127.0.0.1" : null;
        } catch (Throwable e) {
            LOGGER.error("getInetAddress exception", e);
            return getLocal ? "127.0.0.1" : null;
        }
    }

    public static String lookupExtranetIp() {
        if (EXTRANET_IP == null) {
            EXTRANET_IP = getInetAddress(false);
        }
        return EXTRANET_IP;
    }


    public static String headerToString(Map<String, String> headers) {
        if (headers != null && headers.size() > 0) {
            StringBuilder sb = new StringBuilder(headers.size() * 64);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                sb.append(entry.getKey())
                        .append(':')
                        .append(entry.getValue()).append('\n');
            }
            return sb.toString();
        }
        return null;
    }


    public static Map<String, String> headerFromString(String headersString) {
        if (headersString == null) return null;
        Map<String, String> headers = new HashMap<>();
        int L = headersString.length();
        String name, value = null;
        for (int i = 0, start = 0; i < L; i++) {
            char c = headersString.charAt(i);
            if (c != '\n') continue;
            if (start >= L - 1) break;
            String header = headersString.substring(start, i);
            start = i + 1;
            int index = header.indexOf(':');
            if (index <= 0) continue;
            name = header.substring(0, index);
            if (index < header.length() - 1) {
                value = header.substring(index + 1);
            }
            headers.put(name, value);
        }
        return headers;
    }

    public static boolean checkHealth(String ip, int port) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), 1000);
            socket.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static Map<String, Object> getPoolInfo(ThreadPoolExecutor executor) {
        Map<String, Object> info = new HashMap<>(5);
        info.put("corePoolSize", executor.getCorePoolSize());
        info.put("maxPoolSize", executor.getMaximumPoolSize());
        info.put("activeCount(workingThread)", executor.getActiveCount());
        info.put("poolSize(workThread)", executor.getPoolSize());
        info.put("queueSize(blockedTask)", executor.getQueue().size());
        return info;
    }

    public static Map<String, Object> getPoolInfo(EventLoopGroup executors) {
        Map<String, Object> info = new HashMap<>(3);
        int poolSize = 0, queueSize = 0, activeCount = 0;
        for (EventExecutor e : executors) {
            poolSize++;
            if (e instanceof SingleThreadEventLoop) {
                SingleThreadEventLoop executor = (SingleThreadEventLoop) e;
                queueSize += executor.pendingTasks();
                ThreadProperties threadProperties = executor.threadProperties();
                if (threadProperties.state() == Thread.State.RUNNABLE) {
                    activeCount++;
                }
            }
        }
        info.put("poolSize(workThread)", poolSize);
        info.put("activeCount(workingThread)", activeCount);
        info.put("queueSize(blockedTask)", queueSize);
        return info;
    }

}
