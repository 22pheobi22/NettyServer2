package com.sa.service.client;

import com.sa.base.Manager;
import com.sa.net.Packet;
import com.sa.net.PacketHeadInfo;
import com.sa.net.PacketType;
import com.sa.util.Constant;

public class ClientResponebShareHistory extends Packet {

	public ClientResponebShareHistory() {}

	public ClientResponebShareHistory(PacketHeadInfo packetHead) {
		this.setPacketHead(packetHead);
	}

	@Override
	public void execPacket() {
		try {
			/** 设置目标用户的id*/
			this.setToUserId(this.getFromUserId());
			/** 发消息给目标用户*/
			Manager.INSTANCE.sendPacketTo(this, Constant.CONSOLE_CODE_S);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.ClientResponebShareHistory;
	}

}
