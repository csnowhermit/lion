package com.lion.vip.core.handler;

import com.google.common.base.Strings;
import com.lion.vip.api.connection.Connection;
import com.lion.vip.api.connection.SessionContext;
import com.lion.vip.api.event.UserOfflineEvent;
import com.lion.vip.api.event.UserOnlineEvent;
import com.lion.vip.api.protocol.Command;
import com.lion.vip.api.protocol.Packet;
import com.lion.vip.api.spi.Spi;
import com.lion.vip.api.spi.handler.BindValidator;
import com.lion.vip.api.spi.handler.BindValidatorFactory;
import com.lion.vip.common.handler.BaseMessageHandler;
import com.lion.vip.common.message.BindUserMessage;
import com.lion.vip.common.message.ErrorMessage;
import com.lion.vip.common.message.OKMessage;
import com.lion.vip.common.router.RemoteRouter;
import com.lion.vip.common.router.RemoteRouterManager;
import com.lion.vip.core.LionServer;
import com.lion.vip.core.router.LocalRouter;
import com.lion.vip.core.router.LocalRouterManager;
import com.lion.vip.core.router.RouterCenter;
import com.lion.vip.tools.event.EventBus;
import com.lion.vip.tools.log.Logs;

/**
 * 绑定用户处理类
 */
public final class BindUserHandler extends BaseMessageHandler<BindUserMessage> {
    private final BindValidator bindValidator = BindValidatorFactory.create();

    private RouterCenter routerCenter;    //路由中心

    public BindUserHandler(LionServer lionServer) {
        this.routerCenter = lionServer.getRouterCenter();
        this.bindValidator.init(lionServer);
    }

    @Override
    public BindUserMessage decode(Packet packet, Connection connection) {
        return new BindUserMessage(packet, connection);
    }

    @Override
    public void handle(BindUserMessage message) {
        if (message.getPacket().cmd == Command.BIND.cmd) {
            bind(message);
        } else {
            unbind(message);
        }
    }

    /**
     * 注册用户
     *
     * @param message
     */
    public void bind(BindUserMessage message) {
        if (Strings.isNullOrEmpty(message.userId)) {
            ErrorMessage.from(message).setReason("invalid param").close();
            Logs.CONN.error("bind user failure for invalid param, conn={}", message.getConnection());
            return;
        }

        //1.绑定用户时先看下是否握手成功
        SessionContext context = message.getConnection().getSessionContext();
        if (context.handshakeOk()) {
            if (context.userId != null) {
                if (message.userId.equals(context.userId)) {    //处理重复绑定的问题
                    context.tags = message.tags;
                    OKMessage.from(message).setData("bind success").sendRaw();
                    Logs.CONN.info("rebind user success, userId={}, session={}", message.userId, context);
                    return;
                } else {
                    unbind(message);
                }
            }

            //2.验证身份
            boolean success = bindValidator.validate(message.userId, message.data);

            if (success) {
                //3.如果握手成功，就把用户连接注册到路由中心，本地和远程各一份
                success = routerCenter.register(message.userId, message.getConnection());
            }

            if (success) {
                context.userId = message.userId;
                context.tags = message.tags;
                EventBus.post(new UserOnlineEvent(message.getConnection(), message.userId));
                OKMessage.from(message).setData("bind success").sendRaw();
                Logs.CONN.info("bind user success, userId={}, session={}", message.userId, context);
            } else {
                //注册失败的情况：防止本地注册成功，远程注册失败的情况；只有都成功了才叫成功
                routerCenter.unRegister(message.userId, context.getClientType());
                ErrorMessage.from(message).setReason("bind failure").close();
                Logs.CONN.info("bind user failure, userId={}, session={}", message.userId, context);
            }
        } else {
            ErrorMessage.from(message).setReason("not handshake").close();
            Logs.CONN.error("bind user failure not handshake, userId={}, conn={}", message.userId, message.getConnection());
        }
    }

    public void unbind(BindUserMessage message) {
        if (Strings.isNullOrEmpty(message.userId)) {
            ErrorMessage.from(message).setReason("invalid param").close();
            Logs.CONN.error("unbind user failure for invalid param, conn={}", message.getConnection());
            return;
        }

        //1.解绑用户前先判断是否握手成功
        SessionContext context = message.getConnection().getSessionContext();
        if (context.handshakeOk()) {
            //2.先删除远端路由，必须是同一设备才允许解绑
            boolean unRegisterSuccess = true;
            int clientType = context.getClientType();
            String userId = context.userId;
            RemoteRouterManager remoteRouterManager = routerCenter.getRemoteRouterManager();    //得到远端路由管理器
            RemoteRouter remoteRouter = remoteRouterManager.lookup(userId, clientType);
            if (routerCenter != null) {
                String deviceId = remoteRouter.getRouteValue().getDeviceId();
                if (context.deviceId.equals(deviceId)) {    //只有是同一设备的情况下，才允许下线
                    unRegisterSuccess = remoteRouterManager.unregister(userId, clientType);
                }
            }

            //3.再删除本地路由信息
            LocalRouterManager localRouterManager = routerCenter.getLocalRouterManager();
            LocalRouter localRouter = localRouterManager.lookup(userId, clientType);
            if (localRouter != null) {
                String deviceiD = localRouter.getRouteValue().getSessionContext().deviceId;
                if (context.deviceId.equals(deviceiD)) {
                    unRegisterSuccess = localRouterManager.unregister(userId, clientType) && unRegisterSuccess;
                }
            }

            //4.路由删除成功，广播用户下线事件
            if (unRegisterSuccess) {
                context.userId = null;
                context.tags = null;
                EventBus.post(new UserOfflineEvent(message.getConnection(), userId));
                OKMessage.from(message).setData("unbind success").sendRaw();
                Logs.CONN.info("unbind user success, userId={}, session={}", userId, context);
            } else {
                ErrorMessage.from(message).setReason("unbind failed").sendRaw();
                Logs.CONN.error("unbind user failure, unRegister router failure, userId={}, session={}", userId, context);
            }
        } else {
            ErrorMessage.from(message).setReason("no handshake").close();
            Logs.CONN.error("bind user failure not handshake, conn={}", message.getConnection());
        }
    }

    @Spi(order = 1)
    public static class DefaultBindValidatorFactory implements BindValidatorFactory {
        private final BindValidator validator = (userId, data) -> true;

        @Override
        public BindValidator get() {
            return validator;
        }
    }

}
