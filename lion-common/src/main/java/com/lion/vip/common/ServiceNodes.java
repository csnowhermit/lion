package com.lion.vip.common;

import com.lion.vip.api.srd.CommonServiceNode;
import com.lion.vip.api.srd.ServiceNames;
import com.lion.vip.api.srd.ServiceNode;
import com.lion.vip.tools.config.CC;
import com.lion.vip.tools.config.ConfigTools;

/**
 * 供手机端连接的服务器的集合
 */
public class ServiceNodes {

    public static ServiceNode cs() {
        CommonServiceNode node = new CommonServiceNode();
        node.setHost(ConfigTools.getConnectServerRegisterIp());
        node.setPort(CC.lion.net.connect_server_port);
        node.setPersistent(false);
        node.setServiceName(ServiceNames.CONN_SERVER);
        node.setAttrs(CC.lion.net.connect_server_register_attr);

        return node;
    }

    public static ServiceNode ws() {
        CommonServiceNode node = new CommonServiceNode();
        node.setHost(ConfigTools.getConnectServerRegisterIp());
        node.setPort(CC.lion.net.ws_server_port);
        node.setPersistent(false);
        node.setServiceName(ServiceNames.WS_SERVER);
        //node.addAttr(ATTR_PUBLIC_IP, ConfigTools.getPublicIp());
        return node;
    }

    public static ServiceNode gs() {
        CommonServiceNode node = new CommonServiceNode();
        node.setHost(ConfigTools.getGatewayServerRegisterIp());
        node.setPort(CC.lion.net.gateway_server_port);
        node.setPersistent(false);
        node.setServiceName(ServiceNames.GATEWAY_SERVER);
        return node;
    }
}
