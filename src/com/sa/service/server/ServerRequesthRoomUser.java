package com.sa.service.server;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.sa.base.ServerDataPool;
import com.sa.base.element.People;
import com.sa.net.Packet;
import com.sa.net.PacketType;
import com.sa.service.client.ClientResponehRoomUser;

public class ServerRequesthRoomUser extends Packet {

	/**
	 *
	 */
	public ServerRequesthRoomUser() {
	}

	/**
	 * @param transactionId
	 * @param roomId
	 * @param fromUserId
	 * @param toUserId
	 * @param status
	 */
	public ServerRequesthRoomUser(Integer transactionId, String roomId, String fromUserId, String toUserId,
			Integer status) {
		this.setTransactionId(transactionId);
		this.setRoomId(roomId);
		this.setFromUserId(fromUserId);
		this.setToUserId(toUserId);
		this.setStatus(status);
	}

	@Override
	public void execPacket() {

		String[] roomIds = this.getRoomId().split(",");
		if (null != roomIds && roomIds.length > 0) {
			for (String rId : roomIds) {
				/** 根据房间id获取房间内用户信息 */
				Map<String, People> hm = ServerDataPool.dataManager.getRoomUesrs(rId);

				String res = JSONObject.toJSONString(hm);
				/** 实例化获取房间列表 下行 并赋值 */
				ClientResponehRoomUser crru = new ClientResponehRoomUser(this.getPacketHead());
				/** json格式的用户信息放入 选项 1 中 */
				crru.setOption(1, res);
				crru.setOption(2, this.getOption(2));
				crru.setOption(3, this.getOption(3));
				crru.setRoomId(rId);
				/** 执行 */
				crru.execPacket();
			}
		}
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.ServerRequesthRoomUser;
	}

}
