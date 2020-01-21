/**
 *
 * 项目名称：CommunicationManage
 * 类名称：SysGetRoomsReq
 * 类描述：
 * 创建人：Y.P
 * 创建时间：2019年1月29日 下午3:21:52
 * 修改人：Y.P
 * 修改时间：2019年1月29日 下午3:21:52
 * @version  1.0
 *
 */
package com.sa.service.sys;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.sa.base.ServerDataPool;
import com.sa.base.SystemServerManager;
import com.sa.base.element.People;
import com.sa.net.Packet;
import com.sa.net.PacketType;
import com.sa.service.client.ClientMsgReceipt;
import com.sa.util.Constant;

public class SysGetRoomStudentsReq extends Packet {

	public SysGetRoomStudentsReq(){
	}

	@Override
	public void execPacket() {
		int len = Constant.PEOPLE_NUM;
		if(null != this.getOption(1) && !"".equals(this.getOption(1))) {
			len = Integer.parseInt((String) this.getOption(1));
		}

		Map<String, People> map = ServerDataPool.dataManager.getRoomUesrs((String) this.getOption(2));

		if (null == map || 0 == map.size()) {
			ClientMsgReceipt clientMsgReceipt = new ClientMsgReceipt();
			clientMsgReceipt.setPacketHead(this.getPacketHead());
			clientMsgReceipt.setStatus(90101);
			clientMsgReceipt.setOption(254, "房间内暂无用户");
			
			try {
				SystemServerManager.INSTANCE.sendPacketTo(clientMsgReceipt);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return;
		}

		int index = 0;
		String json = "[";
		/** 遍历用户信息*/
		for (Entry<String, People> people : map.entrySet()) {
			if (index%len == 0) json="[";
			index++;

			json += toJson(people);

			if ((index%len == 0 || index==map.size()) && json.length()> 1) {
				json = json.substring(0, json.length()-1);

				json+="]";

				SysGetRoomStudentsRes sysGetRoomStudentsRes = new SysGetRoomStudentsRes();
				sysGetRoomStudentsRes.setPacketHead(this.getPacketHead());
				sysGetRoomStudentsRes.setOption(1, json);
				sysGetRoomStudentsRes.setOption(2, this.getOption(2));

				sysGetRoomStudentsRes.execPacket();
			}
		}
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.SysGetRoomStudentsReq;
	}

	private String toJson(Entry<String, People> people) {
		StringBuffer sb = new StringBuffer();

		sb.append("{\"id\":\""+people.getKey()
				+"\",\"name\":\""+people.getValue().getName()
				+"\",\"icon\":\""+people.getValue().getIcon()
				+"\",\"role\":[");
		for(Iterator iter = people.getValue().getRole().iterator();iter.hasNext();){
			sb.append("\""+(String) iter.next()).append("\",");
        }

		if (0 < people.getValue().getRole().size()) {
			sb.deleteCharAt(sb.length() - 1);
		}

		sb.append("],\"auth\":[");

		for(Map.Entry<String, Integer> auth : people.getValue().getAuth().entrySet()){
			sb.append("{\""+auth.getKey()).append("\":").append(auth.getValue()).append("},");
        }

		if (0 < people.getValue().getAuth().size()) {
			sb.deleteCharAt(sb.length() - 1);
		}

		sb.append("],").append("\"agoraId\":\"").append(people.getValue().getAgoraId()).append("\"");
		sb.append("},");

		return sb.toString();
	}
}
