/**
 * FileName: MessageDispatcher
 * Author:   Ren Xiaotian
 * Date:     2018/11/30 19:57
 */

package com.lion.vip.common;

import com.lion.vip.api.connection.Connection;
import com.lion.vip.api.message.MessageHandler;
import com.lion.vip.api.message.PacketReceiver;
import com.lion.vip.api.protocol.Command;
import com.lion.vip.api.protocol.Packet;
import com.lion.vip.api.spi.push.MessagePusher;
import com.lion.vip.tools.common.Profiler;
import com.lion.vip.tools.log.Logs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.lion.vip.common.ErrorCode.DISPATCH_ERROR;
import static com.lion.vip.common.ErrorCode.UNSUPPORTED_CMD;

public class MessageDispatcher implements PacketReceiver {
    public static final int POLICY_REJECT = 2;
    public static final int POLICY_LOG = 1;
    public static final int POLICY_IGNORE = 0;

    private static final Logger LOGGER = LoggerFactory.getLogger(MessagePusher.class);
    private final Map<Byte, MessageHandler> handlerMap = new HashMap<>();
    private final int unsupportedPolicy;

    public MessageDispatcher(int unsupportedPolicy) {
        this.unsupportedPolicy = unsupportedPolicy;
    }

    public void register(Command command, MessageHandler handler) {
        handlerMap.put(command.cmd, handler);
    }

    public void register(Command command, Supplier<MessageHandler> handlerSupplier, boolean enabled) {
        if (enabled && !handlerMap.containsKey(command.cmd)) {
            register(command, handlerSupplier.get());
        }
    }

    public void register(Command command, Supplier<MessageHandler> handlerSupplier) {
        this.register(command, handlerSupplier, true);
    }

    public MessageHandler unRegister(Command command) {
        return handlerMap.remove(command.cmd);
    }

    @Override
    public void onReceive(Packet packet, Connection connection) {
        MessageHandler handler = handlerMap.get(packet.cmd);
        if (handler != null) {
            Profiler.enter("time cost on [dispatch]");
            try {
                handler.handle(packet, connection);
            } catch (Throwable throwable) {
                LOGGER.error("dispatch message ex, packet={}, connect={}, body={}"
                        , packet, connection, Arrays.toString(packet.body), throwable);
                Logs.CONN.error("dispatch message ex, packet={}, connect={}, body={}, error={}"
                        , packet, connection, Arrays.toString(packet.body), throwable.getMessage());
                ErrorMessage.from(packet, connection)
                        .setErrorCode(DISPATCH_ERROR)
                        .close();
            } finally {
                Profiler.release();
            }
        } else {
            if (unsupportedPolicy > POLICY_IGNORE) {
                Logs.CONN.error("dispatch message failure, cmd={} unsupported, packet={}, connect={}, body={}"
                        , Command.toCMD(packet.cmd), packet, connection);
                if (unsupportedPolicy == POLICY_REJECT) {
                    ErrorMessage
                            .from(packet, connection)
                            .setErrorCode(UNSUPPORTED_CMD)
                            .close();
                }
            }
        }
    }
}
