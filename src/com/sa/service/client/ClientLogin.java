package com.sa.service.client;

import java.util.TreeMap;

import com.sa.base.ServerManager;
import com.sa.net.Packet;
import com.sa.net.PacketHeadInfo;
import com.sa.net.PacketType;
import com.sa.util.Constant;

public class ClientLogin extends Packet {

	public ClientLogin() {}

	public ClientLogin(PacketHeadInfo packetHead) {
		this.setPacketHead(packetHead);
	}

	public ClientLogin(PacketHeadInfo packetHead, TreeMap<Integer, Object> options) {
		this.setPacketHead(packetHead);
		this.setOptions(options);
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.ClientLogin;
	}

	@Override
	public void execPacket() {
		try {
			ServerManager.INSTANCE.sendPacketTo(this, Constant.CONSOLE_CODE_S);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
