/**
 * FileName: PacketEncoder
 * Author:   Ren Xiaotian
 * Date:     2018/11/23 14:12
 */

package com.lion.vip.network.netty.codec;

import com.lion.vip.api.protocol.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import static com.lion.vip.api.protocol.Packet.encodePacket;

/**
 * length(4)+cmd(1)+cc(2)+flags(1)+sessionId(4)+lrc(1)+body(n)
 */
@ChannelHandler.Sharable
public final class PacketEncoder extends MessageToByteEncoder<Packet> {
    public static final PacketEncoder INSTANCE = new PacketEncoder();

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) throws Exception {
        encodePacket(packet, out);
    }
}

