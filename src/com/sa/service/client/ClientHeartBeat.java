package com.sa.service.client;

import com.sa.net.Packet;
import com.sa.net.PacketType;

public class ClientHeartBeat extends Packet{
	public ClientHeartBeat(){}

	@Override
	public PacketType getPacketType() {
		return PacketType.ClientHeartBeat;
	}

	@Override
	public void execPacket() {
	}

}
