package com.sa.service.server;

import com.sa.base.ServerDataPool;
import com.sa.net.Packet;
import com.sa.net.PacketType;
import com.sa.service.client.ClientHeartBeat;

import io.netty.channel.ChannelHandlerContext;

public class ServerHeartBeat extends Packet{

	public ServerHeartBeat() {
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.ServerHearBeat;
	}

	@Override
	public void execPacket() {
		ClientHeartBeat clientHeartBeat = new ClientHeartBeat();
		clientHeartBeat.setRoomId("0");
		clientHeartBeat.setFromUserId(this.getToUserId());
		clientHeartBeat.setToUserId(this.getFromUserId());
		
		ChannelHandlerContext ctx = ServerDataPool.USER_CHANNEL_MAP.get(this.getFromUserId());
		ctx.writeAndFlush(clientHeartBeat);
	}

}
