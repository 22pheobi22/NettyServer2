package com.sa.service.server;

import java.util.List;

import com.sa.base.ServerDataPool;
import com.sa.base.element.Logs;
import com.sa.net.Packet;
import com.sa.net.PacketType;
import com.sa.service.client.ClientResponebRoomChat;

public class ServerRequestbRoomChat extends Packet {
	public ServerRequestbRoomChat(){}

	@Override
	public void execPacket() {
			if (null == this.getOption(1)) {
				this.setOption(1, "0");
			}
			if (null == this.getOption(2)) {
				this.setOption(2, 10);
			}
			//获取多房间聊天记录 给请求人发多条消息 通过roomId区分
			String[] roomIds = this.getRoomId().split(",");
			if (null != roomIds && roomIds.length > 0) {
				for (String rId : roomIds) {
					List<Logs> logList = ServerDataPool.dataManager.getRoomChats(rId, (String) this.getOption(1), Integer.parseInt(this.getOption(2).toString()));

					String json = "[";
					/** 遍历聊天记录*/
					for (Logs logs : logList) {
						json += toJson(logs);
					}

					if (json.length()> 1) {
						json = json.substring(0, json.length()-1);
					}

					json+="]";
					/** 实例化获取房间列表 下行 并赋值*/
					ClientResponebRoomChat crrc = new ClientResponebRoomChat(this.getPacketHead());
					/** json格式的用户信息放入 选项 1 中*/
					crrc.setOption(1, json);
					crrc.setRoomId(rId);
					/** 执行*/
					crrc.execPacket();
				}
			}
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.ServerRequestbRoomChat;
	}

	private String toJson(Logs logs) {
		StringBuffer sb = new StringBuffer();

		sb.append("{\"transactionId\":\""+logs.getTransactionId()
				+"\",\"userId\":\""+logs.getUserId()
				+"\",\"name\":\""+logs.getName()
				+"\",\"icon\":\""+logs.getIco()
				+"\",\"chat\":"+logs.getChat()
				+",\"sendTime\":\""+logs.getSendTime());
		sb.append("\"},");

		return sb.toString();
	}
}
