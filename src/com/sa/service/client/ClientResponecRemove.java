/**
 *
 * 项目名称:[NettyServer]
 * 包:	 [com.sa.service.client]
 * 类名称: [ClientResponecRemove]
 * 类描述: [一句话描述该类的功能]
 * 创建人: [Y.P]
 * 创建时间:[2017年7月18日 上午10:55:50]
 * 修改人: [Y.P]
 * 修改时间:[2017年7月18日 上午10:55:50]
 * 修改备注:[说明本次修改内容]
 * 版本:	 [v1.0]
 *
 */
package com.sa.service.client;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.sa.base.ServerDataPool;
import com.sa.net.Packet;
import com.sa.net.PacketHeadInfo;
import com.sa.net.PacketType;
import com.sa.util.Constant;

public class ClientResponecRemove extends Packet {
	public ClientResponecRemove(){}

	public ClientResponecRemove(PacketHeadInfo packetHead, TreeMap<Integer, Object> options) {
		this.setOptions(options);
		this.setPacketHead(packetHead);
	}

	@Override
	public void execPacket() {
		String[] roomIds = this.getRoomId().split(",");
		if (null != roomIds && roomIds.length > 0) {
			for (String rId : roomIds) {
				/** 发送被迫下线通知*/
				offline(rId);
				/** 通知被踢用户*/
				noticeUser(rId);
			}
		}
		/**该用户是否还存在于其他房间*/
		String userRoomNo = ServerDataPool.dataManager.getUserRoomNo(this.getToUserId());
		if(null==userRoomNo||"".equals(userRoomNo)){
			//若不存在  关闭该服务上用户通道
			ServerDataPool.dataManager.removeUserChannel(this.getToUserId());
		}
		/*//有中心此方法不執行
		*//** 发送被迫下线通知*//*
		offline();
		*//** 移除用户*//*
		ServerDataPool.dataManager.removeRoomUser(this.getRoomId(), this.getToUserId());
		*//** 通知被踢用户*//*
		noticeUser();*/	
		//ServerDataPool.dataManager.removeUserChannel(this.getToUserId());
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.ClientResponecRemove;
	}

	private void offline(String rId) {
		Map<String, Object> result2 = new HashMap<>();

		result2.put("code", 10097);
		result2.put("msg", Constant.ERR_CODE_10097);

		ClientMsgReceipt clientMsgReceipt = new ClientMsgReceipt(this.getPacketHead(), result2);
		clientMsgReceipt.setToUserId(this.getToUserId());
		clientMsgReceipt.setRoomId(rId);
		clientMsgReceipt.execPacket();
	}

	private void noticeUser(String rId) {
		ClientResponebRoomUser crru = new ClientResponebRoomUser(this.getPacketHead());
		crru.setOption(12, this.getToUserId());
		crru.setRoomId(rId);
		crru.execPacket();
	}
}
