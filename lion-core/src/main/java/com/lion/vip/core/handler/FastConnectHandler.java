package com.lion.vip.core.handler;

import com.lion.vip.api.connection.Connection;
import com.lion.vip.api.protocol.Packet;
import com.lion.vip.common.ErrorCode;
import com.lion.vip.common.handler.BaseMessageHandler;
import com.lion.vip.common.message.ErrorMessage;
import com.lion.vip.common.message.FastConnectMessage;
import com.lion.vip.common.message.FastConnectOkMessage;
import com.lion.vip.core.LionServer;
import com.lion.vip.core.session.ReusableSession;
import com.lion.vip.core.session.ReusableSessionManager;
import com.lion.vip.tools.common.Profiler;
import com.lion.vip.tools.config.ConfigTools;
import com.lion.vip.tools.log.Logs;

/**
 * 快速重连的处理类
 */
public class FastConnectHandler extends BaseMessageHandler<FastConnectMessage> {
    private final ReusableSessionManager reusableSessionManager;

    public FastConnectHandler(LionServer lionServer) {
        this.reusableSessionManager = lionServer.getReusableSessionManager();
    }

    @Override
    public FastConnectMessage decode(Packet packet, Connection connection) {
        return new FastConnectMessage(packet, connection);
    }

    @Override
    public void handle(FastConnectMessage message) {
        //从缓存中心查session
        Profiler.enter("time cost on [query session]");
        ReusableSession reusableSession = reusableSessionManager.querySession(message.sessionId);
        Profiler.release();

        //1.如果没查到，说明session已经失效了
        if (reusableSession == null) {
            ErrorMessage.from(message).setErrorCode(ErrorCode.SESSION_EXPIRED).send();
            Logs.CONN.warn("fast connect failure, session is expired, sessionId={}, deviceId={}, conn={}",
                    message.sessionId, message.deviceId, message.getConnection().getChannel());
        } else if (!reusableSession.context.deviceId.equals(message.deviceId)) {
            //2.非法的设备，当前设备不是上次生成session时的设备
            ErrorMessage.from(message).setErrorCode(ErrorCode.INVALID_DEVICE).send();
            Logs.CONN.warn("fast connect failure, not the same device, deviceId={}, session={}, conn={}",
                    message.deviceId, reusableSession.context, message.getConnection().getChannel());
        } else {
            //3.校验成功，重新计算心跳，完成快速重连
            int heartbeat = ConfigTools.getHeartbeat(message.minHeartbeat, message.maxHeartbeat);
            reusableSession.context.setHeartbeat(heartbeat);

            Profiler.enter("time cost on [send FastConnectOkMessage]");

            FastConnectOkMessage.from(message)
                    .setHeartbeat(heartbeat)
                    .sendRaw(f -> {
                        if (f.isSuccess()) {
                            //4.恢复缓存的会话信息，包含会话密钥等
                            message.getConnection().setSessionContext(reusableSession.context);
                            Logs.CONN.info("fast connect success, session={}, conn={}", reusableSession, message.getConnection().getChannel());
                        } else {
                            Logs.CONN.info("fast connect failure, session={}, conn={}", reusableSession, message.getConnection().getChannel());

                        }
                    });
            Profiler.release();
        }
    }
}
