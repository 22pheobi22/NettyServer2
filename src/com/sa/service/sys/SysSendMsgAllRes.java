package com.sa.service.sys;

import com.sa.base.SystemServerManager;
import com.sa.net.Packet;
import com.sa.net.PacketType;

/**
 * 给全部人发消息
 * @author zyh
 *
 * 2020年2月25日
 */
public class SysSendMsgAllRes extends Packet {
	
	public SysSendMsgAllRes(){}

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
		return PacketType.SysSendMsgAllRes;
	}

}
