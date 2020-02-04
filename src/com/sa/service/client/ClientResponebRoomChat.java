package com.sa.service.client;

import com.sa.base.Manager;
import com.sa.net.Packet;
import com.sa.net.PacketHeadInfo;
import com.sa.net.PacketType;
import com.sa.util.Constant;

public class ClientResponebRoomChat extends Packet {

	public ClientResponebRoomChat() {}

	/**
	 * @param transactionId
	 * @param roomId
	 * @param fromUserId
	 * @param toUserId
	 * @param status
	 */
	public ClientResponebRoomChat(PacketHeadInfo packetHead) {
		this.setPacketHead(packetHead);
	}

	@Override
	public void execPacket() {
		try {
			this.setToUserId(getFromUserId());
			Manager.INSTANCE.sendPacketTo(this, Constant.CONSOLE_CODE_S);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.ClientResponebRoomChat;
	}

}
