/**
 *
 * 项目名称:[NettyServer]
 * 包:	 [com.sa.service.client]
 * 类名称: [ClientResponecShareRemove]
 * 类描述: [一句话描述该类的功能]
 * 创建人: [Y.P]
 * 创建时间:[2017年7月16日 下午3:53:24]
 * 修改人: [Y.P]
 * 修改时间:[2017年7月16日 下午3:53:24]
 * 修改备注:[说明本次修改内容]
 * 版本:	 [v1.0]
 *
 */
package com.sa.service.client;

import java.util.TreeMap;

import com.sa.base.Manager;
import com.sa.net.Packet;
import com.sa.net.PacketHeadInfo;
import com.sa.net.PacketType;
import com.sa.util.Constant;

public class ClientResponecShareRemove extends Packet {
	public ClientResponecShareRemove(){}

	public ClientResponecShareRemove(PacketHeadInfo packetHead, TreeMap<Integer, Object> options) {
		this.setPacketHead(packetHead);
		this.setOptions(options);
	}

	@Override
	public void execPacket() {
		try {
			/** 发送给房间内所有用户*/
			Manager.INSTANCE.sendPacketToRoomAllUsers(this, Constant.CONSOLE_CODE_S);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.ClientResponecShareRemove;
	}
}
