package com.sa.service.sys;

import com.sa.base.ConfManager;
import com.sa.base.ServerDataPool;
import com.sa.base.ServerManager;
import com.sa.net.Packet;
import com.sa.net.PacketType;
import com.sa.service.client.ClientResponebRoom;
import com.sa.util.Constant;

/**
 * 房间内消息
 * @author zyh
 *
 * 2020年2月25日
 */
public class SysSendMsgRoomReq extends Packet {
	
	public SysSendMsgRoomReq(){}

	@Override
	public void execPacket() {
		/** 如果有中心 且 目标IP不是中心IP */
		if (ConfManager.getIsCenter()) {
			/** 转发给中心 */
			ServerManager.INSTANCE.sendPacketToCenter(this, Constant.CONSOLE_CODE_TS);
		} else {
			String[] roomIds = this.getRoomId().split(",");
			if (null != roomIds && roomIds.length > 0) {
				for (String rId : roomIds) {
					ServerDataPool.dataManager.setRoomChats(rId,
							System.currentTimeMillis() + "," + this.getTransactionId(), this.getFromUserId(),
							(String) this.getOption(1));
					
					ClientResponebRoom newCrr = new ClientResponebRoom(this.getPacketHead(), this.getOptions());
					newCrr.setRoomId(rId);
					newCrr.execPacket();
				}
			}
		}
		
		SysSendMsgRoomRes res=new SysSendMsgRoomRes();
		res.setPacketHead(this.getPacketHead());
		res.setOption(254, "房间内消息发送成功！");
		res.execPacket();
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.SysSendMsgRoomReq;
	}

}
