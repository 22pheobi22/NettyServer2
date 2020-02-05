/**
 *
 * 项目名称:[NettyServer]
 * 包:	 [com.sa.service.client]
 * 类名称: [ClientResponecShareGet]
 * 类描述: [一句话描述该类的功能]
 * 创建人: [Y.P]
 * 创建时间:[2017年7月11日 下午5:59:29]
 * 修改人: [Y.P]
 * 修改时间:[2017年7月11日 下午5:59:29]
 * 修改备注:[说明本次修改内容]
 * 版本:	 [v1.0]
 *
 */
package com.sa.service.client;

import com.sa.base.Manager;
import com.sa.net.Packet;
import com.sa.net.PacketHeadInfo;
import com.sa.net.PacketType;
import com.sa.util.Constant;

public class ClientResponebShareGet extends Packet {

	public ClientResponebShareGet(){}

	public ClientResponebShareGet(PacketHeadInfo packetHead) {
		this.setPacketHead(packetHead);
	}

	@Override
	public void execPacket() {
		try {
			/** 设置目标用户*/
			this.setToUserId(this.getFromUserId());
			/** 向目标用户发送消息*/
			Manager.INSTANCE.sendPacketTo(this, Constant.CONSOLE_CODE_S);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.ClientResponebShareGet;
	}

}
