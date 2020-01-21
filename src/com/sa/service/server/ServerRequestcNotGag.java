/**
 *
 * 项目名称:[NettyServer]
 * 包:	 [com.sa.service.server]
 * 类名称: [ServerRequestcGag]
 * 类描述: [解除踢人]
 * 创建人: [Y.P]
 * 创建时间:[2017年7月4日 下午5:00:16]
 * 修改人: [Y.P]
 * 修改时间:[2017年7月4日 下午5:00:16]
 * 修改备注:[说明本次修改内容]
 * 版本:	 [v1.0]
 *
 */
package com.sa.service.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sa.base.ConfManager;
import com.sa.base.ServerDataPool;
import com.sa.base.ServerManager;
import com.sa.base.element.People;
import com.sa.base.element.Room;
import com.sa.net.Packet;
import com.sa.net.PacketType;
import com.sa.service.client.ClientMsgReceipt;
import com.sa.service.permission.Permission;
import com.sa.util.Constant;

public class ServerRequestcNotGag extends Packet {
	public ServerRequestcNotGag(){}

	@Override
	public PacketType getPacketType() {
		return PacketType.ServerRequestcNotGag;
	}

	@Override
	public void execPacket() {
		/** 校验用户角色 */
		Set<String> checkRoleSet = new HashSet(){{add(Constant.ROLE_ASSISTANT);}};
		Map<String, Object> result = Permission.INSTANCE.checkUserRole(this.getRoomId(), this.getFromUserId(), checkRoleSet);
		/** 校验成功 */
		if (0 == ((Integer) result.get("code"))) {
			String[] roomIds = this.getRoomId().split(",");
			if (null != roomIds && roomIds.length > 0) {
				for (String rId : roomIds) {
					//this.setRoomId(rId);
					if (null == this.getToUserId() || "".equals(this.getToUserId())) {
						all(rId);
					} else {
						one(this.getToUserId(),rId);
					}
				}
			}
		}
		/** 实例化消息回执 并 赋值 并 执行 */
		new ClientMsgReceipt(this.getPacketHead(), result).execPacket();
	}

	private void one(String userId,String roomId) {
		/** 移除目标用户禁言 */
		People people = ServerDataPool.dataManager.speakAuth(roomId, userId);
		/** 如果目标用户为空 */
		if (null != people) {
			/** 重写返回值 */
			Map<String, Object> result2 = new HashMap<>();

			result2.put("code", 10096);
			result2.put("msg", Constant.ERR_CODE_10096);
			/** 实例化消息回执 并 赋值 并 执行 */
			ClientMsgReceipt cm = new ClientMsgReceipt(this.getPacketHead(), result2);
			cm.setToUserId(userId);
			cm.setRoomId(roomId);
			cm.execPacket();
			/** 如果有中心 并 目标IP不是中心IP */
		} else if (ConfManager.getIsCenter() && !ConfManager.getCenterIp().equals(this.getRemoteIp())) {
			/** 转发到中心 */
			ServerManager.INSTANCE.sendPacketToCenter(this, Constant.CONSOLE_CODE_TS);
		}
	}

	private void all(String roomId) {
		Room room = ServerDataPool.dataManager.getRoom(roomId);

		for (Map.Entry<String, People> entry : room.getPeoples().entrySet()) {
			one(entry.getKey(),roomId);
		}
	}
}
