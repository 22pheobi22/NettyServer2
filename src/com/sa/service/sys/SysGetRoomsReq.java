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

import java.util.Arrays;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.sa.base.ServerDataPool;
import com.sa.base.SystemServerManager;
import com.sa.net.Packet;
import com.sa.net.PacketType;
import com.sa.service.client.ClientMsgReceipt;

public class SysGetRoomsReq extends Packet {

	public SysGetRoomsReq(){
	}

	@Override
	public void execPacket() {
		int len = 50;
		if(null != this.getOption(1) && !"".equals(this.getOption(1))) {
			len = Integer.parseInt((String) this.getOption(1));
		}

		Set<String> roomsSet = ServerDataPool.dataManager.getRooms();
		String[] rooms = roomsSet.toArray(new String[0]);
//		String[] rooms = new String[]{"房间00", "房间01", "房间02", "房间03", "房间04","房间05", "房间06", "房间07", "房间08", "房间09",
//				"房间10", "房间11", "房间12", "房间13", "房间14","房间15", "房间16", "房间17", "房间18", "房间19",
//				"房间20", "房间21", "房间22", "房间23", "房间24","房间25", "房间26", "房间27", "房间28", "房间29"};

		if (null == rooms || 0 == rooms.length) {
			ClientMsgReceipt clientMsgReceipt = new ClientMsgReceipt();
			clientMsgReceipt.setPacketHead(this.getPacketHead());
			clientMsgReceipt.setStatus(90100);
			clientMsgReceipt.setOption(254, "服务内暂无房间");
			
			try {
				SystemServerManager.INSTANCE.sendPacketTo(clientMsgReceipt);
			} catch (Exception e) {
				e.printStackTrace();
			}

			return;
		}
		
		for (int i=0; i<rooms.length; i+=len) {
			if (i+len > rooms.length) {
				len = rooms.length - i;
			}
			
			String[] newData = Arrays.copyOfRange(rooms, i, i+len);
			SysGetRoomsRes sysGetRoomsRes = new SysGetRoomsRes();
			sysGetRoomsRes.setPacketHead(this.getPacketHead());
			sysGetRoomsRes.setOption(1, JSON.toJSONString(newData));
			
			sysGetRoomsRes.execPacket();
		}
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.SysGetRoomsReq;
	}

}
