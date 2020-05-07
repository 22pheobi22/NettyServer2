/**
 *
 * 项目名称:[NettyServer]
 * 包:	 [com.sa.service]
 * 类名称: [ServerRequestbRoom]
 * 类描述: [一句话描述该类的功能]
 * 创建人: [Y.P]
 * 创建时间:[2017年7月4日 上午11:43:47]
 * 修改人: [Y.P]
 * 修改时间:[2017年7月4日 上午11:43:47]
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

public class ClientResponebRoom extends Packet {

	public ClientResponebRoom() {
		this.setOption(253, String.valueOf(System.currentTimeMillis()));
	}

	public ClientResponebRoom(PacketHeadInfo packetHead, TreeMap<Integer, Object> options) {
		this.setOptions(options);
		this.setPacketHead(packetHead);
		this.setOption(1, String.valueOf(this.getOption(1)));
		this.setOption(253, String.valueOf(System.currentTimeMillis()));
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.ClientResponebRoom;
	}

	@Override
	public void execPacket() {
		try {
			String[] roomIds = this.getRoomId().split(",");

			if (null != roomIds && roomIds.length > 0) {
				for (String rId : roomIds) {
					ClientResponebRoom newCrr = new ClientResponebRoom(this.getPacketHead(), this.getOptions());
					newCrr.setRoomId(rId);
					/** 发送给本服务器房间内所有人*/
					Manager.INSTANCE.sendPacketToRoomAllUsers(newCrr, Constant.CONSOLE_CODE_S,newCrr.getFromUserId());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
