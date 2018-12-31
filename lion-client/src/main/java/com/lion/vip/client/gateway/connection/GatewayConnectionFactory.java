package com.lion.vip.client.gateway.connection;

import com.lion.vip.api.connection.Connection;
import com.lion.vip.api.service.BaseService;
import com.lion.vip.api.srd.ServiceListener;
import com.lion.vip.client.LionClient;
import com.lion.vip.common.message.BaseMessage;
import com.lion.vip.tools.config.CC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 网关连接工厂类
 */
public abstract class GatewayConnectionFactory extends BaseService implements ServiceListener {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static GatewayConnectionFactory create(LionClient lionClient) {
        return CC.lion.net.udpGateway() ? new GatewayUDPConnectionFactory(lionClient) : new GatewayTCPConnectionFactory(lionClient);
    }

    abstract public Connection getConnection(String hostAndPort);

    abstract public <M extends BaseMessage> boolean send(String hostAndPort, Function<Connection, M> creator, Consumer<M> sender);

    abstract public <M extends BaseMessage> boolean broadcast(Function<Connection, M> creator, Consumer<M> sender);

}
