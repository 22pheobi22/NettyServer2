/**
 *
 * 项目名称:[NettyServer]
 * 包:	 [com.sa.service.server]
 * 类名称: [ServerRequestbRoomUser]
 * 类描述: [房间内用户列表]
 * 创建人: [Y.P]
 * 创建时间:[2017年7月4日 下午4:59:34]
 * 修改人: [Y.P]
 * 修改时间:[2017年7月4日 下午4:59:34]
 * 修改备注:[]
 * 版本:	 [v1.0]
 *
 */
package com.sa.service.server;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.sa.base.ConfManager;
import com.sa.base.ServerDataPool;
import com.sa.base.ServerManager;
import com.sa.base.element.People;
import com.sa.net.Packet;
import com.sa.net.PacketType;
import com.sa.service.client.ClientResponebRoomTeacher;
import com.sa.util.Constant;

public class ServerRequestbRoomTeacher extends Packet {
	public ServerRequestbRoomTeacher() {
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.ServerRequestbRoomTeacher;
	}

	@Override
	public void execPacket() {

			/** 根据房间id获取房间内普通教师信息 */
			Map<String, People> hm = ServerDataPool.dataManager.getRoomTeachers(this.getRoomId());

			int index = 0;
			String json = "[";
			/** 遍历用户信息 */
			for (Entry<String, People> people : hm.entrySet()) {
				if (index % Constant.PEOPLE_NUM == 0)
					json = "[";
				index++;

				json += toJson(people);

				if ((index % Constant.PEOPLE_NUM == 0 || index == hm.size()) && json.length() > 1) {
					json = json.substring(0, json.length() - 1);

					json += "]";
					/** 实例化获取房间列表 下行 并赋值 */
					ClientResponebRoomTeacher crru = new ClientResponebRoomTeacher(this.getPacketHead());
					/** json格式的用户信息放入 选项 1 中 */
					crru.setOption(1, json);
					// crru.setRoomId(rId);
					/** 执行 */
					crru.execPacket();
				}

			}
	}

	private String toJson(Entry<String, People> people) {
		StringBuffer sb = new StringBuffer();

		sb.append("{\"roomId\":\"" + people.getKey() + "\",\"id\":\"" + people.getValue().getUserId() + "\",\"name\":\"" + people.getValue().getName() + "\",\"icon\":\""
				+ people.getValue().getIcon() + "\",\"role\":[");
		for (Iterator iter = people.getValue().getRole().iterator(); iter.hasNext();) {
			sb.append("\"" + (String) iter.next()).append("\",");
		}

		if (0 < people.getValue().getRole().size()) {
			sb.deleteCharAt(sb.length() - 1);
		}

		sb.append("],\"auth\":[");

		for (Map.Entry<String, Integer> auth : people.getValue().getAuth().entrySet()) {
			sb.append("{\"" + auth.getKey()).append("\":").append(auth.getValue()).append("},");
		}

		if (0 < people.getValue().getAuth().size()) {
			sb.deleteCharAt(sb.length() - 1);
		}

		sb.append("],").append("\"agoraId\":\"").append(people.getValue().getAgoraId()).append("\"");
		sb.append("},");

		return sb.toString();
	}

}
