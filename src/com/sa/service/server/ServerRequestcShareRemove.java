package com.sa.service.server;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sa.base.ConfManager;
import com.sa.base.Manager;
import com.sa.base.ServerDataPool;
import com.sa.net.Packet;
import com.sa.net.PacketType;
import com.sa.service.client.ClientResponecShareRemove;
import com.sa.service.permission.Permission;
import com.sa.util.Constant;

public class ServerRequestcShareRemove extends Packet {
	public ServerRequestcShareRemove(){}

	@Override
	public void execPacket() {
		/** 根据房间id和发信人id校验用户角色 */
		Set<String> checkRoleSet = new HashSet(){{add(Constant.ROLE_TEACHER);add(Constant.ROLE_PARENT_TEACHER);}};
		Map<String, Object> result = Permission.INSTANCE.checkUserRole(this.getRoomId(), this.getFromUserId(), checkRoleSet);
		/** 获取序列 1 的选项 */
		String op1 = (String) this.getOption(1);
		/** 如果选项不空 并 角色校验合格 */
		if (null != op1 && !"".equals(op1) && 0 == ((Integer) result.get("code"))) {
			/** 如果有中心 并 目标地址不是中心地址 */
			if (ConfManager.getIsCenter()) {
				/** 转发到中心 */
				Manager.INSTANCE.sendPacketToCenter(this, Constant.CONSOLE_CODE_TS);
				//本服務器處理
				ClientResponecShareRemove clientResponecShareRemove = new ClientResponecShareRemove(this.getPacketHead(), this.getOptions());
				clientResponecShareRemove.execPacket();
			} else {
				String[] roomIds = this.getRoomId().split(",");
				if (null != roomIds && roomIds.length > 0) {
					for (String rId : roomIds) {
						/** 实例化共享删除 下行 并赋值 并 执行 */
						ClientResponecShareRemove clientResponecShareRemove = new ClientResponecShareRemove(this.getPacketHead(), this.getOptions());
						clientResponecShareRemove.setRoomId(rId);
						clientResponecShareRemove.execPacket();
						/** 删除共享 */
						ServerDataPool.dataManager.removeShare(rId, op1);
					}
				}
			}
		}

		// new ClientMsgReceipt(this.getPacketHead(), result).execPacket();
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.ServerRequestcShareRemove;
	}

}
