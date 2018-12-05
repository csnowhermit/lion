package com.lion.vip.common.message;

import com.lion.vip.api.connection.Connection;
import com.lion.vip.api.protocol.Packet;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

import static com.lion.vip.api.protocol.Command.OK;

/**
 * 确认消息报文
 */
public final class OKMessage extends ByteBufMessage {
    public byte cmd;
    public byte code;
    public String data;

    public OKMessage(byte cmd, Packet message, Connection connection) {
        super(message, connection);
        this.cmd = cmd;
    }

    public OKMessage(Packet message, Connection connection) {
        super(message, connection);
    }

    @Override
    public void decode(ByteBuf body) {
        cmd = decodeByte(body);
        code = decodeByte(body);
        data = decodeString(body);
    }

    @Override
    public void encode(ByteBuf body) {
        encodeByte(body, cmd);
        encodeByte(body, code);
        encodeString(body, data);
    }

    @Override
    public Map<String, Object> encodeJsonBody() {
        Map<String, Object> body = new HashMap<>(3);
        if (cmd > 0) body.put("cmd", cmd);
        if (code > 0) body.put("code", code);
        if (data != null) body.put("data", data);
        return body;
    }

    public static OKMessage from(BaseMessage src) {
        return new OKMessage(src.packet.cmd, src.packet.response(OK), src.connection);
    }

    public OKMessage setCode(byte code) {
        this.code = code;
        return this;
    }

    public OKMessage setData(String data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return "OKMessage{" +
                "data='" + data + '\'' +
                "packet='" + packet + '\'' +
                '}';
    }
}
