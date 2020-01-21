package com.sa.service.client;

import io.netty.channel.ChannelHandlerContext;

import java.util.TreeMap;

import com.sa.base.ConfManager;
import com.sa.base.ServerDataPool;
import com.sa.base.ServerManager;
import com.sa.net.Packet;
import com.sa.net.PacketHeadInfo;
import com.sa.net.PacketType;
import com.sa.util.Constant;

public class ClientLoginOut extends Packet {

	public ClientLoginOut() {}

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
	public void execPacket() {
		try {
			this.setToUserId(this.getFromUserId());
			ServerManager.INSTANCE.sendPacketTo(this, Constant.CONSOLE_CODE_S);
				//如果不是中心
				if(!ConfManager.getCenterId().equals(this.getFromUserId())){
					this.setToUserId("0");
					//轉發到中心只做業務處理 不再往服務器下發消息
					ServerManager.INSTANCE.sendPacketToCenter(this, Constant.CONSOLE_CODE_S);
				}
				
				ChannelHandlerContext ctx =  ServerDataPool.USER_CHANNEL_MAP.get(this.getFromUserId());
				if(null!=ctx){
					ctx.close();
				}
				ServerDataPool.CHANNEL_USER_MAP.remove(ctx);
				ServerDataPool.USER_CHANNEL_MAP.remove(this.getFromUserId());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
