/**
 * FileName: PacketFactory
 * Author:   Ren Xiaotian
 * Date:     2018/12/1 13:25
 */

package com.lion.vip.common.memory;

import com.lion.vip.api.protocol.Command;
import com.lion.vip.api.protocol.Packet;
import com.lion.vip.api.protocol.UDPPacket;
import com.lion.vip.tools.config.CC;

public interface PacketFactory {
    PacketFactory FACTORY = CC.lion.net.udpGateway() ? UDPPacket::new : Packet::new;

    static Packet get(Command command) {
        return FACTORY.create(command);
    }

    Packet create(Command command);

}
