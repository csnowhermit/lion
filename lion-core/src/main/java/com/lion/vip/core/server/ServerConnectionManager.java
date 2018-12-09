package com.lion.vip.core.server;

import com.lion.vip.api.connection.Connection;
import com.lion.vip.api.connection.ConnectionManager;
import com.lion.vip.common.ServerNodes;
import com.lion.vip.network.netty.connection.NettyConnection;
import com.lion.vip.tools.config.CC;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 连接管理器实现类
 */
public final class ServerConnectionManager implements ConnectionManager {

    private final boolean heartbeatcheck;    //是否需要心跳检查

    private ConnectionHolderFactory holderFactory;    //心跳的持有者

    //定义个容器：保存<ChannelID， 连接持有者>的对应关系
    private ConcurrentHashMap<ChannelId, ConnectionHolder> connections = new ConcurrentHashMap<>();
    private ConnectionHolder DEFAULT = new SimpleConnectHolder(null);    //参数为空，是因为默认没有连接


    public ServerConnectionManager(boolean heartbeatcheck) {
        this.heartbeatcheck = heartbeatcheck;
        this.holderFactory = heartbeatcheck ? HeartBeatCheckTask::new : SimpleConnectHolder::new;
    }

    /**
     * 心跳检查任务类：实现ConnectionHolder和TimerTask接口
     */
    private class HeartBeatCheckTask implements ServerConnectionManager.ConnectionHolder, TimerTask {
        private byte timeoutTimes;    //超时周期
        private Connection connection;    //对应的连接

        private HeartBeatCheckTask(byte timeoutTimes, Connection connection) {
            this.timeoutTimes = timeoutTimes;
            this.connection = connection;
        }

        public HeartBeatCheckTask(Connection connection) {
            this.connection = connection;
        }

        private void startTimeout() {
            Connection connection = this.connection;

            //如果连接不是空的，并且是已连接的
            if (Objects.nonNull(connection) && connection.isConnected()) {
                int timeout = connection.getSessionContext().getHeartbeat();    //只有在会话上下文里才会保存心跳信息
            }
        }

        @Override
        public Connection get() {
            return null;
        }

        @Override
        public void close() {
            if (Objects.nonNull(connection)) {
                this.connection.close();
            }
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            Connection connection = this.connection;

            //如果为空，或没有被连接
            if (Objects.isNull(connection) || !connection.isConnected()) {
                //todo add log
                return;
            }

            //如果读超时
            if (connection.isReadTimeout()) {
                //如果超时次数 > 配置中心中配置的最大超时次数
                if (++timeoutTimes > CC.lion.core.max_hb_timeout_times) {
                    connection.close();
                    //todo add log
                } else {
                    //todo add log
                }
            } else {    //不是读超时
                timeoutTimes = 0;
            }
            startTimeout();    //如果不是读超时，timeoutTimes记为0之后重新开始记
        }
    }

    @FunctionalInterface
    private interface ConnectionHolderFactory {
        ConnectionHolder create(Connection connection);
    }

    /**
     * 接口：连接持有者
     */
    private interface ConnectionHolder {
        Connection get();

        void close();
    }

    private static class SimpleConnectHolder implements ServerConnectionManager.ConnectionHolder {
        private Connection connection;

        private SimpleConnectHolder(Connection connection) {
            this.connection = connection;
        }

        public Connection get() {
            return connection;
        }

        public void close() {
            if (Objects.nonNull(connection)) {
                this.connection.close();
            }
        }
    }


    //implements the interface: ConnectionManager


    @Override
    public Connection get(Channel channel) {
        return connections.getOrDefault(channel.id(), DEFAULT).get();
    }

    @Override
    public Connection removeAndClose(Channel channel) {
        ConnectionHolder connectionHolder = connections.remove(channel.id());

        if (Objects.nonNull(connectionHolder)) {
            Connection connection = connectionHolder.get();
            connection.close();
            return connection;
        }

        Connection connection = new NettyConnection();
        connection.init(channel, false);
        connection.close();
        return null;
    }

    @Override
    public void add(Connection connection) {
        connections.putIfAbsent(connection.getChannel().id(), holderFactory.create(connection));
    }

    @Override
    public int getConnNum() {
        return 0;
    }

    @Override
    public void init() {

    }

    @Override
    public void destory() {

    }

}
