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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.sa.base.ConfManager;
import com.sa.base.Manager;
import com.sa.base.ServerDataPool;
import com.sa.base.element.People;
import com.sa.net.Packet;
import com.sa.net.PacketType;
import com.sa.service.client.ClientMsgReceipt;
import com.sa.util.Constant;

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
			//若中心存在，先发往中心
			if (ConfManager.getIsCenter()) {
				/** 消息转发到中心 */
				Manager.INSTANCE.sendPacketToCenter(this, Constant.CONSOLE_CODE_TS);
			} else {
				String[] arr = stuIds.split(",");
				for (String tmp : arr) {
					if ("0".equals(auth)) {
						People people = ServerDataPool.dataManager.notSpeakAuth(roomId, tmp);
						
						if(Objects.nonNull(people)){
							/** 重写返回值*/
							Map<String, Object> result2 = new HashMap<>();

							result2.put("code", 10095);
							result2.put("msg", Constant.ERR_CODE_10095);
							/** 实例化消息回执 并 赋值 并 执行*/
							ClientMsgReceipt cm = new ClientMsgReceipt(this.getPacketHead(), result2);
							cm.setToUserId(tmp);
							cm.setRoomId(roomId);
							cm.execPacket();
						}
					} else if ("1".equals(auth)) {
						People people = ServerDataPool.dataManager.speakAuth(roomId, tmp);
						msg = "解除禁言成功";
						
						/** 如果目标用户为空 */
						if (null != people) {
							/** 重写返回值 */
							Map<String, Object> result2 = new HashMap<>();

							result2.put("code", 10096);
							result2.put("msg", Constant.ERR_CODE_10096);
							/** 实例化消息回执 并 赋值 并 执行 */
							ClientMsgReceipt cm = new ClientMsgReceipt(this.getPacketHead(), result2);
							cm.setToUserId(tmp);
							cm.setRoomId(roomId);
							cm.execPacket();
							/** 如果有中心 并 目标IP不是中心IP */
						}
					}
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