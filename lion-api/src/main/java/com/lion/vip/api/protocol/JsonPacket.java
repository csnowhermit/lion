/**
 * FileName: JsonPacket
 * Author:   Ren Xiaotian
 * Date:     2018/11/21 14:22
 */

package com.lion.vip.api.protocol;

import com.lion.vip.api.Constants;
import com.lion.vip.api.spi.common.Json;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.Map;

/**
 * json数据包
 */
public class JsonPacket extends Packet {
    public Map<String, Object> body;

    public JsonPacket() {
        super(Command.UNKNOWN);
        this.addFlag(FLAG_JSON_BODY);
    }

    public JsonPacket(Command cmd) {
        super(cmd);
        this.addFlag(FLAG_JSON_BODY);
    }

    public JsonPacket(Command cmd, int sessionId) {
        super(cmd, sessionId);
        this.addFlag(FLAG_JSON_BODY);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getBody() {
        return body;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void setBody(T body) {
        this.body = (Map<String, Object>) body;
    }

    @Override
    public int getBodyLength() {
        return body == null ? 0 : body.size();
    }

    @Override
    public Packet response(Command command) {
        return new JsonPacket(command, sessionId);
    }

    @Override
    public Object toFrame(Channel channel) {
        byte[] json = Json.JSON.toJson(this).getBytes(Constants.UTF_8);
        return new TextWebSocketFrame(Unpooled.wrappedBuffer(json));
    }

    @Override
    public String toString() {
        return "JsonPacket{" +
                "cmd=" + cmd +
                ", cc=" + cc +
                ", flags=" + flags +
                ", sessionId=" + sessionId +
                ", lrc=" + lrc +
                ", body=" + body +
                '}';
    }



}
