/**
 *
 * 项目名称:[NettyCenter]
 * 包:	 [com.sa.service.client]
 * 类名称: [COffline]
 * 类描述: [一句话描述该类的功能]
 * 创建人: [Y.P]
 * 创建时间:[2017年7月17日 下午4:40:27]
 * 修改人: [Y.P]
 * 修改时间:[2017年7月17日 下午4:40:27]
 * 修改备注:[说明本次修改内容]
 * 版本:	 [v1.0]
 *
 */
package com.sa.service.client;

import java.util.TreeMap;

import com.sa.base.ConfManager;
import com.sa.base.ServerManager;
import com.sa.net.Packet;
import com.sa.net.PacketHeadInfo;
import com.sa.net.PacketType;
import com.sa.util.Constant;

public class COffline extends Packet {
	public COffline(){}

	public COffline(PacketHeadInfo packetHead, TreeMap<Integer, Object> options) {
		this.setPacketHead(packetHead);
		this.setOptions(options);
	}

	@Override
	public void execPacket() {
		if(ConfManager.getCenterId().contains(this.getFromUserId())) {
			ClientMsgReceipt mr = new ClientMsgReceipt(this.getTransactionId(), this.getRoomId(), this.getFromUserId(), 10098);
			mr.setOption(254, Constant.ERR_CODE_10098);

			try {
				ServerManager.INSTANCE.sendPacketTo(mr, Constant.CONSOLE_CODE_S);
			} catch (Exception e) {
				e.printStackTrace();
			}

			ServerManager.INSTANCE.ungisterUserId(mr.getToUserId());
		}
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.COffline;
	}

}
