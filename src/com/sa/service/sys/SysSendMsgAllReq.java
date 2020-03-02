package com.sa.service.sys;

import com.sa.base.ConfManager;
import com.sa.base.Manager;
import com.sa.net.Packet;
import com.sa.net.PacketType;
import com.sa.service.client.ClientResponebAll;
import com.sa.util.Constant;

/**
 * 给全部人发消息
 * @author zyh
 *
 * 2020年2月25日
 */
public class SysSendMsgAllReq extends Packet {
	
	public SysSendMsgAllReq(){}

	@Override
	public void execPacket() {
		/** 如果有中心 并且 中心不是目标地址*/
		if (ConfManager.getIsCenter()) {
			/** 转发到中心*/
			Manager.INSTANCE.sendPacketToCenter(this, Constant.CONSOLE_CODE_TS);
		}else{
			/** 实例化 发送全体消息 下行 并执行*/
			ClientResponebAll clientResponebAll = new ClientResponebAll(this.getPacketHead(), this.getOptions());
			clientResponebAll.execPacket();
		}
		
		SysSendMsgAllRes res=new SysSendMsgAllRes();
		res.setPacketHead(this.getPacketHead());
		res.setOption(254, "1v1消息发送成功！");
		res.execPacket();
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.SysSendMsgAllReq;
	}

}
