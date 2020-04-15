package com.sa.service.client;

import java.util.TreeMap;

import com.sa.base.Manager;
import com.sa.base.ServerDataPool;
import com.sa.net.Packet;
import com.sa.net.PacketHeadInfo;
import com.sa.net.PacketType;
import com.sa.util.Constant;

import io.netty.channel.ChannelHandlerContext;

public class ClientLoginOut extends Packet {	

	public ClientLoginOut() {
	}

	public ClientLoginOut(PacketHeadInfo packetHead) {
		this.setPacketHead(packetHead);
	}

	public ClientLoginOut(PacketHeadInfo packetHead, TreeMap<Integer, Object> options) {
		this.setPacketHead(packetHead);
		this.setOptions(options);
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.ClientLoginOut;
	}

	@Override
	public void execPacket() {//中心才执行
		try {
			this.setToUserId(this.getFromUserId());
			Manager.INSTANCE.sendPacketTo(this, Constant.CONSOLE_CODE_S);

			ChannelHandlerContext ctx = ServerDataPool.USER_CHANNEL_MAP.get(this.getFromUserId());
			if (null != ctx) {
				ctx.close();
			}
			ServerDataPool.CHANNEL_USER_MAP.remove(ctx);
			ServerDataPool.USER_CHANNEL_MAP.remove(this.getFromUserId());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
