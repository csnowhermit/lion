package com.lion.vip.network.netty.udp;

import com.lion.vip.api.message.PacketReceiver;
import com.lion.vip.api.protocol.Packet;
import com.lion.vip.network.netty.codec.PacketDecoder;
import com.lion.vip.network.netty.connection.NettyConnection;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;

public class UDPChannelHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(UDPChannelHandler.class);
    private final NettyConnection nettyConnection = new NettyConnection();
    private final PacketReceiver packetReceiver;
    private InetAddress multicastAddress;
    private NetworkInterface networkInterface;


    public UDPChannelHandler(PacketReceiver packetReceiver) {
        this.packetReceiver = packetReceiver;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        nettyConnection.init(ctx.channel(), false);
        if (multicastAddress != null) {
            ((DatagramChannel) ctx.channel()).joinGroup(multicastAddress, networkInterface, null).addListener(future -> {
                if (future.isSuccess()) {
                    LOGGER.info("join multicast group success, channel={}, group={}", ctx.channel(), multicastAddress);
                } else {
                    LOGGER.error("join multicast group error, channel={}, group={}", ctx.channel(), multicastAddress, future.cause());
                }
            });
        }
        LOGGER.info("init udp channel = {}", ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        nettyConnection.close();
        if (multicastAddress != null) {
            ((DatagramChannel) ctx.channel()).leaveGroup(multicastAddress, networkInterface, null).addListener(future -> {
                if (future.isSuccess()) {
                    LOGGER.info("leave multicast group success, channel={}, group={}", ctx.channel(), multicastAddress);
                } else {
                    LOGGER.error("leave multicast group error, channel={}, group={}", ctx.channel(), multicastAddress, future.cause());
                }
            });
        }
        LOGGER.info("disconnect udp channel = {}, connection = {}", ctx.channel(), nettyConnection);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        DatagramPacket datagramPacket = (DatagramPacket) msg;
        Packet packet = PacketDecoder.decodeFrame(datagramPacket);
        packetReceiver.onReceive(packet, nettyConnection);
        datagramPacket.release();    //最后一个使用方药释放引用
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        nettyConnection.close();
        LOGGER.error("udp handler cauht an exception, channel = {}, connection = {}", ctx.channel(), nettyConnection);
    }

    public UDPChannelHandler setMulticastAddress(InetAddress multicastAddress) {
        if (!multicastAddress.isMulticastAddress()) {
            throw new IllegalArgumentException(multicastAddress + "not a multicastAddress");
        }

        this.multicastAddress = multicastAddress;
        return this;
    }

    public UDPChannelHandler setNetworkInterface(NetworkInterface networkInterface) {
        this.networkInterface = networkInterface;
        return this;
    }

    public NettyConnection getConnection() {
        return nettyConnection;
    }

}
