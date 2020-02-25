package com.sa.service.sys;

import com.sa.base.SystemServerManager;
import com.sa.net.Packet;
import com.sa.net.PacketType;

/**
 * 房间内消息
 * @author zyh
 *
 * 2020年2月25日
 */
public class SysSendMsgRoomRes extends Packet {
	
	public SysSendMsgRoomRes(){}

	@Override
	public void execPacket() {
		try {
			SystemServerManager.INSTANCE.sendPacketTo(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.SysSendMsgRoomRes;
	}

}
