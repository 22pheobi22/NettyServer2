/**
 *
 * 项目名称:[NettyServer]
 * 包:	 [com.sa.service.client]
 * 类名称: [ClientResponebRoomUser]
 * 类描述: [一句话描述该类的功能]
 * 创建人: [Y.P]
 * 创建时间:[2017年7月4日 下午5:00:53]
 * 修改人: [Y.P]
 * 修改时间:[2017年7月4日 下午5:00:53]
 * 修改备注:[说明本次修改内容]
 * 版本:	 [v1.0]
 *
 */
package com.sa.service.client;

import java.util.Iterator;
import java.util.Map;

import com.sa.base.Manager;
import com.sa.base.ServerDataPool;
import com.sa.base.element.People;
import com.sa.net.Packet;
import com.sa.net.PacketHeadInfo;
import com.sa.net.PacketType;
import com.sa.util.Constant;

public class ClientResponebRoomUser extends Packet {

	public ClientResponebRoomUser() {}

	public ClientResponebRoomUser(PacketHeadInfo packetHead) {
		this.setPacketHead(packetHead);
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.ClientResponebRoomUser;
	}

	@Override
	public void execPacket() {
		try {
			/** 如果 用户增量 不为空*/
			if (null != this.getOption(11)) {
				People people = ServerDataPool.dataManager.getRoomUesr(this.getRoomId(), this.getFromUserId());
				//People people = ServerDataPool.serverDataManager.getRoomUesr(this.getRoomId(), this.getFromUserId());
				if (null != people)
					toJson(people);
				/** 向房间内全员发送消息*/
				Manager.INSTANCE.sendPacketToRoomAllUsers(this, Constant.CONSOLE_CODE_S, this.getFromUserId());
			/** 如果 用户减量不为空*/
			} else if(null != this.getOption(12)){
				/** 向房间内全员发送消息*/
				Manager.INSTANCE.sendPacketToRoomAllUsers(this, Constant.CONSOLE_CODE_S,this.getFromUserId());
			} else {
				/** 发消息给目标用户*/
				Manager.INSTANCE.sendPacketTo(this, Constant.CONSOLE_CODE_S);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String toJson(People people) {
		StringBuffer sb = new StringBuffer();

		sb.append("{\"id\":\""+this.getFromUserId()
				+"\",\"name\":\""+people.getName()
				+"\",\"icon\":\""+people.getIcon()
				+"\",\"role\":[");
		for(Iterator iter = people.getRole().iterator();iter.hasNext();){
			sb.append("\""+(String) iter.next()).append("\",");
        }

		if (0 < people.getRole().size()) {
			sb.deleteCharAt(sb.length() - 1);
		}

		sb.append("],\"auth\":[");

		for(Map.Entry<String, Integer> auth : people.getAuth().entrySet()){
			sb.append("{\""+auth.getKey()).append("\":").append(auth.getValue()).append("},");
        }

		if (0 < people.getAuth().size()) {
			sb.deleteCharAt(sb.length() - 1);
		}

		sb.append("],").append("\"agoraId\":\"").append(people.getAgoraId()).append("\"");
		sb.append("}");

		this.setOption(11, sb.toString());

		return sb.toString();
	}
}