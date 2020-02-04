/**
 *
 * 项目名称:[NettyServer]
 * 包:	 [com.sa.service.client]
 * 类名称: [ClientResponebOne]
 * 类描述: [一句话描述该类的功能]
 * 创建人: [Y.P]
 * 创建时间:[2017年7月4日 下午12:03:18]
 * 修改人: [Y.P]
 * 修改时间:[2017年7月4日 下午12:03:18]
 * 修改备注:[说明本次修改内容]
 * 版本:	 [v1.0]
 *
 */
package com.sa.service.client;

import java.util.TreeMap;

import com.sa.base.ServerManager;
import com.sa.net.Packet;
import com.sa.net.PacketHeadInfo;
import com.sa.net.PacketType;
import com.sa.util.Constant;

public class ClientResponebOne extends Packet {

	public ClientResponebOne(){
		this.setOption(253, String.valueOf(System.currentTimeMillis()));
	}

	public ClientResponebOne(PacketHeadInfo packetHead, TreeMap<Integer, Object> options) {
		this.setOptions(options);
		this.setPacketHead(packetHead);
		this.setOption(253, String.valueOf(System.currentTimeMillis()));
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.ClientResponebOne;
	}

	@Override
	public void execPacket() {
		try {
			/** 发送消息给目标用户*/
			ServerManager.INSTANCE.sendPacketTo(this, Constant.CONSOLE_CODE_S);

			/*String userId = this.getToUserId();
			if (userId.endsWith("APP")) {
				userId = userId.replace("APP", "");
			} else {
				userId += "APP";
			}
			ChannelHandlerContext ctx = ServerDataPool.USER_CHANNEL_MAP.get(userId);
			if (null != ctx) {
				ServerManager.INSTANCE.sendPacketTo(this, ctx, Constant.CONSOLE_CODE_S);
			}*/
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
