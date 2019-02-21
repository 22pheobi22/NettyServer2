package com.sa.service.server;

import java.util.Map;

import com.sa.base.ConfManager;
import com.sa.base.ServerDataPool;
import com.sa.base.ServerManager;
import com.sa.net.Packet;
import com.sa.net.PacketType;
import com.sa.service.client.ClientResponecShareRemove;
import com.sa.service.permission.Permission;
import com.sa.util.Constant;

public class ServerRequestcShareRemove extends Packet {
	public ServerRequestcShareRemove(){}

	@Override
	public void execPacket() {
		/** 根据房间id和发信人id校验用户角色*/
		Map<String, Object> result = Permission.INSTANCE.checkUserRole(this.getRoomId(), this.getFromUserId(), Constant.ROLE_TEACHER);
		/** 获取序列 1 的选项*/
		String op1 = (String) this.getOption(1);
		/** 如果选项不空 并 角色校验合格*/
		if (null != op1 && !"".equals(op1) && 0 == ((Integer) result.get("code"))) {
			/** 如果有中心 并 目标地址不是中心地址*/
			if (ConfManager.getIsCenter() && !ConfManager.getCenterIp().equals(this.getRemoteIp())) {
				/** 转发到中心*/
				ServerManager.INSTANCE.sendPacketToCenter(this, Constant.CONSOLE_CODE_TS);
			} else {
				/** 实例化共享删除 下行 并赋值 并 执行*/
				new ClientResponecShareRemove(this.getPacketHead(), this.getOptions()).execPacket();
				/** 删除共享*/
				ServerDataPool.serverDataManager.removeShare(this.getRoomId(), op1);
			}
		}

//		new ClientMsgReceipt(this.getPacketHead(), result).execPacket();
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.ServerRequestcShareRemove;
	}

}