/**
 * 
 * 项目名称:[NettyServer]
 * 包:	 [com.sa.service.server]
 * 类名称: [ServerRequestApplyAuth]
 * 类描述: [一句话描述该类的功能]
 * 创建人: [Y.P]
 * 创建时间:[2018年7月30日 上午10:05:23]
 * 修改人: [Y.P]
 * 修改时间:[2018年7月30日 上午10:05:23]
 * 修改备注:[说明本次修改内容]  
 * 版本:	 [v1.0]   
 * 
 */
package com.sa.service.server;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sa.base.ConfManager;
import com.sa.base.Manager;
import com.sa.net.Packet;
import com.sa.net.PacketType;
import com.sa.service.client.ClientMsgReceipt;
import com.sa.service.client.ClientResponecAgreeApplyAuth;
import com.sa.service.permission.Permission;
import com.sa.util.Constant;

public class ServerRequestcAgreeApplyAuth extends Packet {
	public ServerRequestcAgreeApplyAuth(){}

	@Override
	public PacketType getPacketType() {
		return PacketType.ServerRequestcAgreeApplyAuth;
	}

	@Override
	public void execPacket() {
		/** 校验用户角色 */
		Set<String> checkRoleSet = new HashSet(){{add(Constant.ROLE_TEACHER);add(Constant.ROLE_PARENT_TEACHER);}};
		Map<String, Object> result = Permission.INSTANCE.checkUserRole(this.getRoomId(), this.getFromUserId(),checkRoleSet);

		/** 实例化消息回执 并 赋值 并 执行 */
		new ClientMsgReceipt(this.getPacketHead(), result).execPacket();

		/** 如果校验成功 */
		if (0 == ((Integer) result.get("code"))) {
			/** 如果有中心 并 目标IP不是中心IP */
			if (ConfManager.getIsCenter()) {
				/** 消息转发到中心 */
				Manager.INSTANCE.sendPacketToCenter(this, Constant.CONSOLE_CODE_TS);
			} else {
				String[] roomIds = this.getRoomId().split(",");
				if (null != roomIds && roomIds.length > 0) {
					for (String rId : roomIds) {
						/** 实例化 开课 下行 并 赋值 并 执行 */
						ClientResponecAgreeApplyAuth clientResponecAgreeApplyAuth = new ClientResponecAgreeApplyAuth(
								this.getPacketHead(), this.getOptions());
						clientResponecAgreeApplyAuth.setRoomId(rId);
						clientResponecAgreeApplyAuth.execPacket();
					}
				}
			}
		}
	}

}