package com.lion.vip.core.handler;

import com.google.common.base.Strings;
import com.lion.vip.api.connection.Connection;
import com.lion.vip.api.connection.SessionContext;
import com.lion.vip.api.protocol.Packet;
import com.lion.vip.common.handler.BaseMessageHandler;
import com.lion.vip.common.message.ErrorMessage;
import com.lion.vip.common.message.HandshakeMessage;
import com.lion.vip.common.message.HandshakeOkMessage;
import com.lion.vip.common.security.AESCipher;
import com.lion.vip.common.security.CipherBox;
import com.lion.vip.core.LionServer;
import com.lion.vip.core.session.ReusableSession;
import com.lion.vip.core.session.ReusableSessionManager;
import com.lion.vip.tools.config.ConfigTools;
import com.lion.vip.tools.log.Logs;

import static com.lion.vip.common.ErrorCode.REPEAT_HANDSHAKE;

/**
 * 握手消息类
 */
public final class HandshakeHandler extends BaseMessageHandler<HandshakeMessage> {

    private ReusableSessionManager reusableSessionManager;    //可重复使用的session管理器

    public HandshakeHandler(LionServer lionServer) {
        this.reusableSessionManager = lionServer.getReusableSessionManager();
    }


    @Override
    public HandshakeMessage decode(Packet packet, Connection connection) {
        return new HandshakeMessage(packet, connection);
    }

    @Override
    public void handle(HandshakeMessage message) {
        if (message.getConnection().getSessionContext().isSecurity()) {
            doSecurity(message);
        } else {
            doInSecurity(message);
        }
    }

    /**
     * 加密握手
     *
     * @param message
     */
    private void doSecurity(HandshakeMessage message) {
        byte[] iv = message.iv;//AES密钥向量16位
        byte[] clientKey = message.clientKey;//客户端随机数16位
        byte[] serverKey = CipherBox.I.randomAESKey();//服务端随机数16位
        byte[] sessionKey = CipherBox.I.mixKey(clientKey, serverKey);//会话密钥16位

        //1.校验客户端消息字段
        if (Strings.isNullOrEmpty(message.deviceId)
                || iv.length != CipherBox.I.getAesKeyLength()
                || clientKey.length != CipherBox.I.getAesKeyLength()) {
            ErrorMessage.from(message).setReason("Param invalid").close();
            Logs.CONN.error("handshake failure, message={}, conn={}", message, message.getConnection());
            return;
        }

        //2.重复握手判断
        SessionContext context = message.getConnection().getSessionContext();
        if (message.deviceId.equals(context.deviceId)) {
            ErrorMessage.from(message).setErrorCode(REPEAT_HANDSHAKE).send();
            Logs.CONN.warn("handshake failure, repeat handshake, conn={}", message.getConnection());
            return;
        }

        //3.更换会话密钥RSA=>AES(clientKey)
        context.changeCipher(new AESCipher(clientKey, iv));

        //4.生成可复用session, 用于快速重连
        ReusableSession session = reusableSessionManager.genSession(context);

        //5.计算心跳时间
        int heartbeat = ConfigTools.getHeartbeat(message.minHeartbeat, message.maxHeartbeat);

        //6.响应握手成功消息
        HandshakeOkMessage
                .from(message)
                .setServerKey(serverKey)
                .setHeartbeat(heartbeat)
                .setSessionId(session.sessionId)
                .setExpireTime(session.expireTime)
                .send(f -> {
                            if (f.isSuccess()) {
                                //7.更换会话密钥AES(clientKey)=>AES(sessionKey)
                                context.changeCipher(new AESCipher(sessionKey, iv));
                                //8.保存client信息到当前连接
                                context.setOsName(message.osName)
                                        .setOsVersion(message.osVersion)
                                        .setClientVersion(message.clientVersion)
                                        .setDeviceId(message.deviceId)
                                        .setHeartbeat(heartbeat);

                                //9.保存可复用session到Redis, 用于快速重连
                                reusableSessionManager.cacheSession(session);

                                Logs.CONN.info("handshake success, conn={}", message.getConnection());
                            } else {
                                Logs.CONN.info("handshake failure, conn={}", message.getConnection(), f.cause());
                            }
                        }
                );
    }

    /**
     * 不加密握手
     *
     * @param message
     */
    private void doInSecurity(HandshakeMessage message) {
        //1.校验客户端消息字段
        if (Strings.isNullOrEmpty(message.deviceId)) {
            ErrorMessage.from(message).setReason("Param invalid").close();
            Logs.CONN.error("handshake failure, message={}, conn={}", message, message.getConnection());
            return;
        }

        //2.重复握手判断
        SessionContext context = message.getConnection().getSessionContext();
        if (message.deviceId.equals(context.deviceId)) {
            ErrorMessage.from(message).setErrorCode(REPEAT_HANDSHAKE).send();
            Logs.CONN.warn("handshake failure, repeat handshake, conn={}", message.getConnection());
            return;
        }

        //6.响应握手成功消息
        HandshakeOkMessage.from(message).send();

        //8.保存client信息到当前连接
        context.setOsName(message.osName)
                .setOsVersion(message.osVersion)
                .setClientVersion(message.clientVersion)
                .setDeviceId(message.deviceId)
                .setHeartbeat(Integer.MAX_VALUE);

        Logs.CONN.info("handshake success, conn={}", message.getConnection());
    }


}
