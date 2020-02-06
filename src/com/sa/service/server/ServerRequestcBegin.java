package com.sa.service.server;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sa.base.ConfManager;
import com.sa.base.Manager;
import com.sa.net.Packet;
import com.sa.net.PacketType;
import com.sa.service.client.ClientMsgReceipt;
import com.sa.service.client.ClientResponecBegin;
import com.sa.service.permission.Permission;
import com.sa.util.Constant;
/**
 * 开课
 *
 */
public class ServerRequestcBegin extends Packet {
	public ServerRequestcBegin(){}

	@Override
	public void execPacket() {
		/** 校验用户角色*/
		Set<String> checkRoleSet = new HashSet<String>(){{add(Constant.ROLE_TEACHER);add(Constant.ROLE_PARENT_TEACHER);}};
		Map<String, Object> result = Permission.INSTANCE.checkUserRole(this.getRoomId(), this.getFromUserId(),checkRoleSet);
		/** 如果校验成功*/
		if (0 == ((Integer) result.get("code"))) {
			/** 如果有中心 并 目标IP不是中心IP*/
			if (ConfManager.getIsCenter()) {
				/** 消息转发到中心*/
				Manager.INSTANCE.sendPacketToCenter(this, Constant.CONSOLE_CODE_TS);
			} else {
//				HashSet<String> roomRoles = getRole(this.getOption(1));
//				ServerDataPool.serverDataManager.setRoomRole(this.getRoomId(), roomRoles, ConfManager.getRoomChat());
				String[] roomIds = this.getRoomId().split(",");
				if (null != roomIds && roomIds.length > 0) {
					for (String rId : roomIds) {
						/** 实例化 开课 下行 并 赋值 并 执行*/
						ClientResponecBegin clientResponecBegin = new ClientResponecBegin(this.getPacketHead());
						clientResponecBegin.setRoomId(rId);
						clientResponecBegin.execPacket();
					}
				}
			}
		}
		/** 实例化消息回执 并 赋值 并 执行*/
		new ClientMsgReceipt(this.getPacketHead(), result).execPacket();
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.ServerRequestcBegin;
	}

}
