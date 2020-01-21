/**
 *
 * 项目名称：CommunicationManage
 * 类名称：SysCloseRoomsReq
 * 类描述：
 * 创建人：Y.P
 * 创建时间：2019年2月12日 下午3:36:20
 * 修改人：Y.P
 * 修改时间：2019年2月12日 下午3:36:20
 * @version  1.0
 *
 */
package com.sa.service.sys;

import com.sa.base.ServerDataPool;
import com.sa.base.element.People;
import com.sa.net.Packet;
import com.sa.net.PacketType;

public class SysChangeSpeakStatusReq extends Packet {

	public SysChangeSpeakStatusReq(){
	}

	@Override
	public void execPacket() {
		String roomId = (String) this.getOption(1);
		String stuIds = (String) this.getOption(2);
		String auth = (String) this.getOption(3);

		String msg = "禁言成功";
		if (null != stuIds) {
			String[] arr = stuIds.split(",");
			for (String tmp : arr) {
				if ("0".equals(auth)) {
					People people = ServerDataPool.dataManager.notSpeakAuth(roomId, tmp);
				} else if ("1".equals(auth)) {
					People people = ServerDataPool.dataManager.speakAuth(roomId, tmp);
					msg = "解除禁言成功";
				}
			}
		}
		
		SysChangeSpeakStatusRes sysChangeSpeakStatusRes = new SysChangeSpeakStatusRes();
		sysChangeSpeakStatusRes.setPacketHead(this.getPacketHead());
		sysChangeSpeakStatusRes.setOption(1, roomId);
		sysChangeSpeakStatusRes.setOption(254, msg);
		
		sysChangeSpeakStatusRes.execPacket();
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.SysChangeSpeakStatusReq;
	}

}