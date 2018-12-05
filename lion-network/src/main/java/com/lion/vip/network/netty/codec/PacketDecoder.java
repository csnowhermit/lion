package com.lion.vip.network.netty.codec;

import com.lion.vip.api.protocol.JsonPacket;
import com.lion.vip.api.protocol.Packet;
import com.lion.vip.api.protocol.UDPPacket;
import com.lion.vip.tools.Jsons;
import com.lion.vip.tools.config.CC;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.TooLongFrameException;

import java.util.List;

import static com.lion.vip.api.protocol.Packet.decodePacket;

/**
 * length(4)+cmd(1)+cc(2)+flags(1)+sessionId(4)+lrc(1)+body(n)
 */
@ChannelHandler.Sharable
public class PacketDecoder extends ByteToMessageDecoder {
    private static final int maxPacketSize = CC.lion.core.max_packet_size;    //数据包的最大大小

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        decodeHeartbeat(in, out);
        decodeFrames(in, out);
    }

    private void decodeFrames(ByteBuf in, List<Object> out) {
        if (in.readableBytes() >= Packet.HEADER_LEN) {
            //1.记录当前读取位置。如果读到不完整的frame，要恢复到该位置，便于下次读取
            in.markReaderIndex();

            Packet packet = decodeFrame(in);
            if (packet != null) {
                out.add(packet);
            } else {
                //2.读到不完整的frame，恢复到最近一次正常读取的位置，便于下次读取
                in.resetReaderIndex();
            }
        }
    }

    private Packet decodeFrame(ByteBuf in) {
        int readAbleBytes = in.readableBytes();
        int bodyLength = in.readInt();

        if (readAbleBytes < (bodyLength + Packet.HEADER_LEN)) {    //如果读到的长度不够，则直接返回null
            return null;
        }
        if (bodyLength > maxPacketSize) {    //如果读到的长度超限，则抛出异常
            throw new TooLongFrameException("packet body length over limit:" + bodyLength);
        }
        return decodePacket(new Packet(in.readByte()), in, bodyLength);
    }

    public static Packet decodeFrame(DatagramPacket datagramPacket) {
        ByteBuf in = datagramPacket.content();
        int readAbleBytes = in.readableBytes();
        int bodyLength = in.readInt();
        if (readAbleBytes < (bodyLength + Packet.HEADER_LEN)) {
            return null;
        }

        return decodePacket(new UDPPacket(in.readByte(), datagramPacket.sender()), in, bodyLength);
    }

    public static Packet decodeFrame(String frame) {
        if (frame == null) {
            return null;
        }
        return Jsons.fromJson(frame, JsonPacket.class);
    }

    private void decodeHeartbeat(ByteBuf in, List<Object> out) {
        while (in.isReadable()) {
            if (in.readByte() == Packet.HB_PACKET_BYTE) {
                out.add(Packet.HB_PACKET);
            } else {
                in.readerIndex(in.readerIndex() - 1);
                break;
            }
        }
    }
}
